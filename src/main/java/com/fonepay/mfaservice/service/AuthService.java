package com.fonepay.mfaservice.service;

import com.fonepay.mfaservice.config.JwtUtils;
import com.fonepay.mfaservice.dto.LoginRequest;
import com.fonepay.mfaservice.dto.LoginResponse;
import com.fonepay.mfaservice.dto.RegisterRequest;
import com.fonepay.mfaservice.dto.RegisterResponse;
import com.fonepay.mfaservice.entity.User;
import com.fonepay.mfaservice.repository.UserRepository;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private String secret;
    @Value("${qr_issuer}")
    private String issuer;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public ResponseEntity<RegisterResponse> register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername().trim())) {
            RegisterResponse response = new RegisterResponse();
            response.setMessage(request.getUsername() + ", Username already exists");
            response.setImageBase64(null);

            return ResponseEntity.badRequest().body(response);
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );
        SecretGenerator secretGenerator = new DefaultSecretGenerator();
        secret = secretGenerator.generate();

        user.setSecretKey(secret);
        userRepository.save(user);

        // Create QR Code URL (for Google Authenticator)
        QrData data = new QrData.Builder()
                .label(request.getEmail())
                .secret(secret)
                .issuer(issuer)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        QrGenerator qrGenerator = new ZxingPngQrGenerator();
        String qrUri = null;
        try {
            qrUri = Utils.getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
            String base64Image = qrUri.substring(qrUri.indexOf(",") + 1);

            RegisterResponse registerResponse = new RegisterResponse();
            registerResponse.setMessage("Successfully registered");
            registerResponse.setImageBase64(base64Image);
            return ResponseEntity.ok(registerResponse);
        } catch (QrGenerationException e) {
            RegisterResponse registerResponse = new RegisterResponse();
            registerResponse.setMessage("Failed to generate QR code");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(registerResponse);
        }
    }

    public ResponseEntity<LoginResponse> authenticate(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User userDetails = (User) authentication.getPrincipal();
        String jwtToken = jwtUtils.generateToken(authentication);

        return ResponseEntity.ok(LoginResponse.builder()
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .accessToken(jwtToken)
                .build());
    }
}

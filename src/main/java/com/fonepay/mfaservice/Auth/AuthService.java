package com.fonepay.mfaservice.Auth;

import com.fonepay.mfaservice.config.JwtUtils;
import com.fonepay.mfaservice.entity.User;
import com.fonepay.mfaservice.repository.UserRepository;
import com.fonepay.mfaservice.secretKey.SecretKeyGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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
    private final SecretKeyGenerator secretKeyGenerator;
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
        secret = secretKeyGenerator.generateSecret();

        user.setSecretKey(secret);
        userRepository.save(user);

        // Create QR Code URL (for Google Authenticator)
        var data = new QrData.Builder()
                .label(request.getEmail())
                .secret(secret)
                .issuer(issuer)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        QrGenerator qrGenerator = new ZxingPngQrGenerator();
        String qrUrl = null;
        try {
            qrUrl = Utils.getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
            String base64Image = qrUrl.substring(qrUrl.indexOf(",") + 1);

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
}

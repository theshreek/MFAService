package com.fonepay.mfaservice.service;

import com.fonepay.mfaservice.dto.TotpResponse;
import com.fonepay.mfaservice.entity.Totp;
import com.fonepay.mfaservice.entity.User;
import com.fonepay.mfaservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base32;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TotpService {
    private final UserRepository userRepository;

    public String generateOtp() throws NoSuchAlgorithmException, InvalidKeyException {
        // Time step (30 seconds)
        long timeStep = 30;
        long time = Instant.now().getEpochSecond() / timeStep;

        String base32Secret = getSecretKey();
        // Decode base32 secret
        Base32 base32 = new Base32();
        byte[] secretKey = base32.decode(base32Secret);

        // Convert time counter to byte[8]
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(time);
        byte[] timeBytes = buffer.array();

        // Create HMAC-SHA1
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey, "HmacSHA1");
        mac.init(keySpec);
        byte[] hash = mac.doFinal(timeBytes);

        // Dynamic truncation to get a 6-digit code
        int offset = hash[hash.length - 1] & 0xF;
        int binary =
                ((hash[offset] & 0x7F) << 24) |
                        ((hash[offset + 1] & 0xFF) << 16) |
                        ((hash[offset + 2] & 0xFF) << 8) |
                        (hash[offset + 3] & 0xFF);

        int otp = binary % 1_000_000;
        return String.format("%06d", otp);
    }

    public ResponseEntity<TotpResponse> verifyTotp(Totp totp) throws NoSuchAlgorithmException, InvalidKeyException {
        String generateOtp = generateOtp();
        String requestOtp = totp.getOtp();
        TotpResponse totpResponse = new TotpResponse();
        if (generateOtp.equals(requestOtp)) {
            totpResponse.setStatus("Success");

            return ResponseEntity.ok(totpResponse);
        }
        totpResponse.setStatus("Failed");
        return ResponseEntity.ok(totpResponse);
    }

    public String getSecretKey() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            User user1 = user.get();
            String secretKey = user1.getSecretKey();
            return secretKey;
        }
        return null;
    }
}

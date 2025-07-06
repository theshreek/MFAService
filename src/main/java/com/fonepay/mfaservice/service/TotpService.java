package com.fonepay.mfaservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TotpService {
    public static String generateTOTP() throws NoSuchAlgorithmException, InvalidKeyException {
        // Time step (30 seconds)
        long timeStep = 30;
        long time = Instant.now().getEpochSecond() / timeStep;

        //TODO getTotpByEmail();

        // Decode base32 secret
//        Base32 base32 = new Base32();
//        byte[] secretKey = base32.decode(base32Secret);

        // Convert time counter to byte[8]
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(time);
        byte[] timeBytes = buffer.array();

        // Create HMAC-SHA1
        Mac mac = Mac.getInstance("HmacSHA1");
//        SecretKeySpec keySpec = new SecretKeySpec(secretKey, "HmacSHA1");
//        mac.init(keySpec);
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

//    public ResponseEntity<?> verifyOtp(Totp totp) {
//        String requestOtp = totp.getOtp();
////        String generateOtp = generateTOTP();
//
//    }

//    public String getTotp(){
//
//    }
}

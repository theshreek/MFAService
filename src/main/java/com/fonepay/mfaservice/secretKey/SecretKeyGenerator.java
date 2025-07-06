package com.fonepay.mfaservice.secretKey;

import org.apache.commons.codec.binary.Base32;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;

@Configuration
public class SecretKeyGenerator {
    private final SecureRandom randomBytes = new SecureRandom();
    private final int numCharacters=32;
    private static final Base32 encoder = new Base32();

    private byte[] getRandomBytes() {

        byte[] bytes = new byte[numCharacters * 5 / 8];
        this.randomBytes.nextBytes(bytes);
        return bytes;
    }

    public String generateSecret() {
        return new String(encoder.encode(this.getRandomBytes()));
    }
}

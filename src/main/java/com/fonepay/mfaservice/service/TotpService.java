package com.fonepay.mfaservice.service;

import com.fonepay.mfaservice.dto.TotpResponse;
import com.fonepay.mfaservice.entity.Totp;
import com.fonepay.mfaservice.entity.User;
import com.fonepay.mfaservice.repository.UserRepository;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.NtpTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TotpService {

    private final UserRepository userRepository;

    public ResponseEntity<TotpResponse> verifyTotp(Totp totp) throws UnknownHostException {

        TimeProvider timeProvider = new NtpTimeProvider("pool.ntp.org");
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        DefaultCodeVerifier  verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        verifier.setAllowedTimePeriodDiscrepancy(0);

        String secret = getSecretKey();
        String code = totp.getOtp();
        boolean successful = verifier.isValidCode(secret, code);

        TotpResponse totpResponse = new TotpResponse();
        if (successful) {
            totpResponse.setStatus("Success");

            return ResponseEntity.ok(totpResponse);
        }
        totpResponse.setStatus("Failed");
        return ResponseEntity.ok(totpResponse);
    }

    public String getSecretKey() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            return user.getSecretKey();
        }
        return null;
    }
}

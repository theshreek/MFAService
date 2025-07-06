package com.fonepay.mfaservice.controller;

import com.fonepay.mfaservice.dto.TotpResponse;
import com.fonepay.mfaservice.entity.Totp;
import com.fonepay.mfaservice.service.TotpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/v1/totp")
public class TotpController {

    @Autowired
    TotpService totpService;
    @PostMapping("/verify")
    public ResponseEntity<TotpResponse> verify(@RequestBody Totp totp) throws NoSuchAlgorithmException, InvalidKeyException {
        return totpService.verifyTotp(totp);
    }
}

package com.fonepay.mfaservice.controller;

import com.fonepay.mfaservice.dto.TotpResponse;
import com.fonepay.mfaservice.entity.Totp;
import com.fonepay.mfaservice.service.TotpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/totp")
@RequiredArgsConstructor
public class TotpController {

    private final TotpService totpService;

    @PostMapping("/verify")
    public ResponseEntity<TotpResponse> verify(@RequestBody Totp totp) throws UnknownHostException {
        return totpService.verifyTotp(totp);
    }}

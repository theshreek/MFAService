package com.fonepay.mfaservice.controller;

import com.fonepay.mfaservice.service.AuthService;
import com.fonepay.mfaservice.dto.LoginRequest;
import com.fonepay.mfaservice.dto.LoginResponse;
import com.fonepay.mfaservice.dto.RegisterRequest;
import com.fonepay.mfaservice.dto.RegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginRequest request) {
        return authService.authenticate(request);
    }

}

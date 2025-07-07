package com.fonepay.mfaservice.dto;

import lombok.Data;

@Data
public class RegisterResponse {
    private String message;
    private String imageBase64;
}

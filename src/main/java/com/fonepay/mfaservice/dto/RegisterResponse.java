package com.fonepay.mfaservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterResponse {
    private String message;
    private String imageBase64;
}

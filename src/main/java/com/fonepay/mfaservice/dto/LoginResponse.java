package com.fonepay.mfaservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private int id;
    private String username;
    private String email;
    private String accessToken;
}

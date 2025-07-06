package com.fonepay.mfaservice.dto;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class TotpResponse {
    private String status;
}

package com.system.brands.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpResponse {
    private String token;
    private String username;
}

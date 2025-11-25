package com.system.brands.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginDto {
    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

}

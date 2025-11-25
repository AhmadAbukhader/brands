package com.system.brands.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Brand request data")
public class BrandRequestDto {
    
    @NotBlank(message = "Brand name is required")
    @JsonProperty("name")
    @Schema(description = "Brand name", example = "Nike")
    private String name;
}


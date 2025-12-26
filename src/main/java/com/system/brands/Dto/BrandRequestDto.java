package com.system.brands.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "Brand name", example = "Nike", required = true)
    private String name;

    @Schema(description = "Brand name in English", example = "Nike")
    private String nameEnglish;
}

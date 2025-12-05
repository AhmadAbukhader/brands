package com.system.brands.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product request data")
public class ProductRequestDto {

    @NotNull(message = "Brand ID is required")
    @JsonProperty("brandId")
    @Schema(description = "Brand ID", example = "1")
    private Integer brandId;

    @JsonProperty("categoryId")
    @Schema(description = "Category ID", example = "1")
    private Integer categoryId;

    @NotBlank(message = "Product name is required")
    @JsonProperty("name")
    @Schema(description = "Product name", example = "Air Max 90")
    private String name;
}

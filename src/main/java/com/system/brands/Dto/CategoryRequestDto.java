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
@Schema(description = "Category request data")
public class CategoryRequestDto {

    @NotBlank(message = "Category name is required")
    @JsonProperty("name")
    @Schema(description = "Category name", example = "Electronics")
    private String name;
}

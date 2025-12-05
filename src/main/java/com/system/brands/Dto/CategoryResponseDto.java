package com.system.brands.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Category response data")
public class CategoryResponseDto {

    @JsonProperty("id")
    @Schema(description = "Category ID", example = "1")
    private Integer id;

    @JsonProperty("name")
    @Schema(description = "Category name", example = "Electronics")
    private String name;
}

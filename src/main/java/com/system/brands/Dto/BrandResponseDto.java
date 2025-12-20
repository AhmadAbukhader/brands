package com.system.brands.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Brand response data")
public class BrandResponseDto {

    @JsonProperty("id")
    @Schema(description = "Brand ID", example = "1")
    private Integer id;

    @JsonProperty("name")
    @Schema(description = "Brand name", example = "Nike")
    private String name;

    @JsonProperty("image")
    @Schema(description = "Brand image as base64 encoded string")
    private String image;

    @JsonProperty("imageUrl")
    @Schema(description = "URL to fetch brand image", example = "/api/brands/1/image")
    private String imageUrl;

    @JsonProperty("products")
    @Schema(description = "List of products under this brand")
    private List<ProductResponseDto> products;
}

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
@Schema(description = "Product response data")
public class ProductResponseDto {

    @JsonProperty("id")
    @Schema(description = "Product ID", example = "1")
    private Integer id;

    @JsonProperty("brandId")
    @Schema(description = "Brand ID", example = "1")
    private Integer brandId;

    @JsonProperty("brandName")
    @Schema(description = "Brand name", example = "Nike")
    private String brandName;

    @JsonProperty("categoryId")
    @Schema(description = "Category ID", example = "1")
    private Integer categoryId;

    @JsonProperty("categoryName")
    @Schema(description = "Category name", example = "Electronics")
    private String categoryName;

    @JsonProperty("name")
    @Schema(description = "Product name", example = "Air Max 90")
    private String name;

    @JsonProperty("imageUrl")
    @Schema(description = "S3 URL to product image", example = "https://brands-bucket-818.s3.us-east-1.amazonaws.com/products/uuid.png")
    private String imageUrl;

    @JsonProperty("productOrder")
    @Schema(description = "Product order for sorting", example = "1")
    private Integer productOrder;
}

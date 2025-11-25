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
    
    @JsonProperty("name")
    @Schema(description = "Product name", example = "Air Max 90")
    private String name;
    
    @JsonProperty("quantity")
    @Schema(description = "Product quantity", example = "100")
    private String quantity;
    
    @JsonProperty("packaging")
    @Schema(description = "Product packaging", example = "Box")
    private String packaging;
    
    @JsonProperty("unit")
    @Schema(description = "Product unit", example = "Piece")
    private String unit;
}


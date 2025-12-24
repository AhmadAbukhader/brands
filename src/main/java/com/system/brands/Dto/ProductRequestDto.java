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
@Schema(description = "Product request data")
public class ProductRequestDto {

    @Schema(description = "Brand ID", example = "1", required = true)
    private Integer brandId;

    @Schema(description = "Category ID", example = "1")
    private Integer categoryId;

    @Schema(description = "Product name", example = "Air Max 90", required = true)
    private String name;

    @Schema(description = "Product packaging information", example = "Box of 12 units")
    private String packaging;

    @Schema(description = "Whether the product is new", example = "true")
    private Boolean isNew;

    @Schema(description = "Whether the product is hidden", example = "false")
    private Boolean isHidden;
}

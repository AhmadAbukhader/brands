package com.system.brands.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product order request data")
public class ProductOrderRequestDto {

    @NotNull(message = "Product ID is required")
    @JsonProperty("productId")
    @Schema(description = "Product ID to reorder", example = "1")
    private Integer productId;

    @NotNull(message = "New order position is required")
    @JsonProperty("newOrder")
    @Schema(description = "New order position", example = "5")
    private Integer newOrder;
}

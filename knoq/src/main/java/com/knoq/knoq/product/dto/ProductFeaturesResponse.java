package com.knoq.knoq.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Product features grouped for display")
public record ProductFeaturesResponse(
        @Schema(example = "[\"Classic\", \"Luxury\", \"Refined\"]")
        List<String> style,

        @Schema(example = "[\"Interior slip pocket\", \"Zipper compartment\"]")
        List<String> composition,

        @Schema(example = "[\"Detachable leather strap\", \"Adjustable strap\"]")
        List<String> usage
) {
    public ProductFeaturesResponse {
        style = List.copyOf(style);
        composition = List.copyOf(composition);
        usage = List.copyOf(usage);
    }
}

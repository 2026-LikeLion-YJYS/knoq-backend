package com.knoq.knoq.needs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateNeedsAnalysisRequest(
        @NotBlank
        @Size(max = 100)
        @Schema(example = "토트백 / 쇼퍼백")
        String productCategory,

        @NotBlank
        @Size(max = 100)
        @Schema(example = "Black · Cognac")
        String preferredColor,

        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "Visetos|Leather|Canvas|Nylon")
        @Schema(example = "Leather", allowableValues = {"Visetos", "Leather", "Canvas", "Nylon"})
        String preferredMaterial,

        @NotBlank
        @Size(max = 50)
        @Schema(example = "Medium · Large")
        String preferredSize
) {
}

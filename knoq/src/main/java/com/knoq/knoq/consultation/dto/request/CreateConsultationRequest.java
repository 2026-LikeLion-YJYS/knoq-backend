package com.knoq.knoq.consultation.dto.request;

import com.knoq.knoq.consultation.entity.HelpType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateConsultationRequest(
        @NotNull
        @Schema(example = "PRODUCT_COMPARISON")
        HelpType helpType,

        @NotNull
        @Size(max = 3)
        @Schema(example = "[\"prod_12\", \"prod_33\"]")
        List<@NotBlank String> productIds,

        @Schema(example = "true")
        boolean includeNeedsAnalysis
) {
}

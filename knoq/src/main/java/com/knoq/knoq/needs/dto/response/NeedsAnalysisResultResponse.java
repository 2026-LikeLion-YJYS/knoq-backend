package com.knoq.knoq.needs.dto.response;

import com.knoq.knoq.needs.entity.NeedsAnalysis;
import com.knoq.knoq.needs.support.PreferredMaterialNormalizer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NeedsAnalysisResultResponse {

    private String productCategory;
    private String preferredColor;
    @Schema(example = "Visetos", allowableValues = {"Visetos", "Leather", "Canvas", "Nylon"})
    private String preferredMaterial;
    private String preferredSize;
    private String comment;
    private LocalDateTime analyzedAt;

    public static NeedsAnalysisResultResponse from(NeedsAnalysis needsAnalysis) {
        return NeedsAnalysisResultResponse.builder()
                .productCategory(needsAnalysis.getProductCategory())
                .preferredColor(needsAnalysis.getPreferredColor())
                .preferredMaterial(PreferredMaterialNormalizer.normalize(needsAnalysis.getPreferredMaterial()))
                .preferredSize(needsAnalysis.getPreferredSize())
                .comment(needsAnalysis.getComment())
                .analyzedAt(needsAnalysis.getAnalyzedAt())
                .build();
    }
}

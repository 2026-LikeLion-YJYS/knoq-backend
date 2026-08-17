package com.knoq.knoq.needs.dto.response;

import com.knoq.knoq.needs.entity.NeedsAnalysis;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NeedsAnalysisResultResponse {

    private String productCategory;
    private String preferredColor;
    private String preferredMaterial;
    private String preferredSize;
    private String comment;
    private LocalDateTime analyzedAt;

    public static NeedsAnalysisResultResponse from(NeedsAnalysis needsAnalysis) {
        return NeedsAnalysisResultResponse.builder()
                .productCategory(needsAnalysis.getProductCategory())
                .preferredColor(needsAnalysis.getPreferredColor())
                .preferredMaterial(needsAnalysis.getPreferredMaterial())
                .preferredSize(needsAnalysis.getPreferredSize())
                .comment(needsAnalysis.getComment())
                .analyzedAt(needsAnalysis.getAnalyzedAt())
                .build();
    }
}
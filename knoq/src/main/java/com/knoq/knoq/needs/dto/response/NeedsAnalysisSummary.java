package com.knoq.knoq.needs.dto.response;

import com.knoq.knoq.needs.entity.NeedsAnalysis;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NeedsAnalysisSummary {

    @Schema(example = "아우터")
    private String productCategory;
    @Schema(example = "블랙")
    private String preferredColor;
    @Schema(example = "울")
    private String preferredMaterial;
    @Schema(example = "M")
    private String preferredSize;
    @Schema(example = "저장하신 제품들은 주로 울 소재를 선호하시는 경향이 있습니다.")
    private String comment;

    public static NeedsAnalysisSummary from(NeedsAnalysis needsAnalysis) {
        return NeedsAnalysisSummary.builder()
                .productCategory(needsAnalysis.getProductCategory())
                .preferredColor(needsAnalysis.getPreferredColor())
                .preferredMaterial(needsAnalysis.getPreferredMaterial())
                .preferredSize(needsAnalysis.getPreferredSize())
                .comment(needsAnalysis.getComment())
                .build();
    }
}
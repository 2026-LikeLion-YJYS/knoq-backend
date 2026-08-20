package com.knoq.knoq.needs.dto.response;

import com.knoq.knoq.needs.entity.NeedsAnalysis;
import com.knoq.knoq.needs.support.PreferredMaterialNormalizer;
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
    @Schema(example = "Visetos", allowableValues = {"Visetos", "Leather", "Canvas", "Nylon"})
    private String preferredMaterial;
    @Schema(example = "M")
    private String preferredSize;
    @Schema(example = "저장하신 제품들은 주로 울 소재를 선호하시는 경향이 있습니다.")
    private String comment;

    public static NeedsAnalysisSummary from(NeedsAnalysis needsAnalysis) {
        return of(
                needsAnalysis.getProductCategory(),
                needsAnalysis.getPreferredColor(),
                needsAnalysis.getPreferredMaterial(),
                needsAnalysis.getPreferredSize(),
                needsAnalysis.getComment()
        );
    }

    public static NeedsAnalysisSummary of(
            String productCategory,
            String preferredColor,
            String preferredMaterial,
            String preferredSize,
            String comment
    ) {
        return NeedsAnalysisSummary.builder()
                .productCategory(productCategory)
                .preferredColor(preferredColor)
                .preferredMaterial(PreferredMaterialNormalizer.normalize(preferredMaterial))
                .preferredSize(preferredSize)
                .comment(comment)
                .build();
    }
}

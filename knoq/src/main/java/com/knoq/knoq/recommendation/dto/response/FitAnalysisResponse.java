package com.knoq.knoq.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record FitAnalysisResponse(
        @Schema(example = "미니멀 라이프스타일과 잘 어울리는 제품이에요.") String summary,
        @Schema(example = "[\"심플한 디자인이 미니멀한 취향과 잘 맞아요.\"]") List<String> reasons,
        @Schema(example = "[\"실제 색상과 소재는 매장에서 확인해 주세요.\"]") List<String> cautions
) {
    public FitAnalysisResponse {
        reasons = List.copyOf(reasons);
        cautions = List.copyOf(cautions);
    }
}

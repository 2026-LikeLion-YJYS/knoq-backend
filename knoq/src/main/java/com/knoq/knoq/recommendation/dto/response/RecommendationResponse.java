package com.knoq.knoq.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record RecommendationResponse(
        @Schema(example = "미니멀 라이프스타일에 어울리는 제품 3개를 추천했어요.") String summary,
        List<RecommendedProductResponse> products
) {
}

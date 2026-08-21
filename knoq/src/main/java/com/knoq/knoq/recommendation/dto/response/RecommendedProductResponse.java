package com.knoq.knoq.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record RecommendedProductResponse(
        @Schema(example = "sav_11") String savedProductId,
        @Schema(example = "prod_1") String productId,
        @Schema(example = "미니멀 스타일과 잘 어울리는 심플한 디자인의 제품이에요.") String reason
) {
}

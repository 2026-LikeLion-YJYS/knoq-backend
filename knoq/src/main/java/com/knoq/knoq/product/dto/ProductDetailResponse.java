package com.knoq.knoq.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ProductDetailResponse(
        @Schema(example = "prod_1")
        String productId,

        @Schema(example = "Tracy 비세토스 호보")
        String name,

        @Schema(example = "비세토스 모노그램 캔버스 + 나파 가죽")
        String material,

        @Schema(example = "길이 조절 및 탈부착 가능한 가죽 스트랩, 로고 락 클로저, 내부 슬립 포켓, 지퍼 수납공간")
        String features,

        @Schema(example = "1690000")
        Long price,

        @Schema(example = "[\"L\", \"11 x 33 x 31 cm\"]")
        List<String> size,

        @Schema(example = "[\"Cognac\"]")
        List<String> color,

        @Schema(example = "/demo/products/prod_1/front.png")
        String thumbnailUrl,

        @Schema(example = "클래식한 모노그램과 가죽 트림이 돋보이는 포멀한 호보백입니다.")
        String aiGenerated
) {}

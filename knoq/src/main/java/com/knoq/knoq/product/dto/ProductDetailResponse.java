package com.knoq.knoq.product.dto;

import java.util.List;

// 브랜드 공식 설명 / 특징(features)은 화면에 안 쓰기로 해서 응답에서 제외 (AI 요약 aiGenerated로 대체)
public record ProductDetailResponse(
        String productId,
        String name,
        String material,
        Long price,
        List<String> size,
        List<String> color,
        String aiGenerated
) {}
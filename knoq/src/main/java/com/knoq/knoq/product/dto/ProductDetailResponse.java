package com.knoq.knoq.product.dto;

import java.util.List;

// 브랜드 공식 설명 / 특징(features)은 화면에 안 쓰기로 해서 응답에서 제외 (AI 요약 aiGenerated로 대체)
// images: FR-200 등록용으로 찍어둔 정면/측면/윗면 사진(base64)을 그대로 재사용.
// 등록할 때 "정면 → 측면 → 윗면" 순서로 호출하는 게 규칙이라 배열 순서 = [정면, 측면, 윗면]으로 취급.
// 별도 이미지 URL이 아니라 base64 원본 문자열이라, 프론트에서 data:image/jpeg;base64, 접두어 붙여서 써야 함.
// prod_1~3처럼 등록 안 한 제품은 빈 배열로 내려가니, 그 경우 thumbnailUrl로 대체 표시.
public record ProductDetailResponse(
        String productId,
        String name,
        String material,
        Long price,
        List<String> size,
        List<String> color,
        String aiGenerated,
        List<String> images
) {}
package com.knoq.knoq.product.dto;

import java.util.List;

public record ProductDetailResponse(
        String material,
        Long price,
        List<String> size,
        List<String> color,
        Descriptions descriptions
) {
    public record Descriptions(
            String brandOfficial,
            String aiGenerated
    ) {}
}
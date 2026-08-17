package com.knoq.knoq.needs.service;

import java.util.List;

public record ProductAttributes(
        String productId,
        String category,   // TODO: Product에 category 필드가 없어 항상 null. A 확인 필요.
        String material,
        List<String> colors,
        List<String> sizes
) {}
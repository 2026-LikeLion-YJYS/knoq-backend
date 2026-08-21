package com.knoq.knoq.needs.service;

import java.util.List;

public record ProductAttributes(
        String productId,
        String name,
        String category,
        String material,
        List<String> colors,
        List<String> sizes,
        String features,
        List<String> styles,
        List<String> compositions,
        List<String> usages
) { }

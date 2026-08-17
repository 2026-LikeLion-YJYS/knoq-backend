package com.knoq.knoq.needs.service;

import java.util.List;

public record ProductAttributes(
        String productId,
        String category,
        String material,
        List<String> colors,
        List<String> sizes
) {}

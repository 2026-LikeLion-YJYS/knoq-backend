package com.knoq.knoq.needs.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

final class NeedsAnalysisAggregator {

    private NeedsAnalysisAggregator() {}

    static String mostFrequent(List<String> values) {
        return values.stream()
                .filter(Objects::nonNull)
                .filter(v -> !v.isBlank())
                .collect(Collectors.groupingBy(v -> v, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    static String buildComment(String color, String material, String size) {
        List<String> parts = new ArrayList<>();
        if (material != null) parts.add(material + " 소재");
        if (color != null) parts.add(color + " 계열");
        if (size != null) parts.add(size + " 사이즈");

        if (parts.isEmpty()) {
            return "저장하신 제품들에서 공통된 특징을 찾지 못했습니다.";
        }
        return "저장하신 제품들은 주로 " + String.join(", ", parts) + "를 선호하시는 경향이 있습니다.";
    }

    static String buildSelectionComment(String category, String color, String material, String size) {
        List<String> attributes = new ArrayList<>();
        if (color != null && !color.isBlank()) attributes.add(color + " 컬러");
        if (material != null && !material.isBlank()) attributes.add(material + " 소재");
        if (size != null && !size.isBlank()) attributes.add(size + " 사이즈");

        String target = category == null || category.isBlank() ? "제품" : category;
        if (attributes.isEmpty()) {
            return "고객님은 " + target + " 제품을 선호하고 계세요.";
        }
        return "고객님은 " + String.join(", ", attributes)
                + " 특성을 갖춘 " + target + " 제품을 선호하고 계세요.";
    }
}

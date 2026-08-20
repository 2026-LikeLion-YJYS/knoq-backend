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
}

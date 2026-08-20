package com.knoq.knoq.needs.support;

import java.util.Locale;

public final class PreferredMaterialNormalizer {

    private PreferredMaterialNormalizer() {
    }

    /**
     * 제품의 자유 형식 소재명을 프론트 선택지와 동일한 표준값으로 변환한다.
     * 비세토스는 캔버스나 가죽과 혼합된 제품이 많으므로 가장 먼저 판별한다.
     */
    public static String normalize(String material) {
        if (material == null || material.isBlank()) {
            return null;
        }

        String normalized = material.toLowerCase(Locale.ROOT);
        if (containsAny(normalized, "visetos", "비세토스")) {
            return "Visetos";
        }
        if (containsAny(normalized, "leather", "레더", "가죽", "나파")) {
            return "Leather";
        }
        if (containsAny(normalized, "canvas", "캔버스")) {
            return "Canvas";
        }
        if (containsAny(normalized, "nylon", "나일론")) {
            return "Nylon";
        }
        return null;
    }

    private static boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}

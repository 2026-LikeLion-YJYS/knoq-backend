package com.knoq.knoq.needs.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;

class PreferredMaterialNormalizerTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "'비세토스 모노그램 캔버스 + 나파 가죽', Visetos",
            "'Visetos coated canvas', Visetos",
            "'이탈리아산 풀그레인 레더', Leather",
            "'나파 가죽', Leather",
            "'코튼 캔버스', Canvas",
            "Nylon, Nylon",
            "'나일론 100%', Nylon"
    })
    @DisplayName("제품 소재 원문을 프론트 선택 옵션으로 표준화한다")
    void normalize_material(String material, String expected) {
        assertThat(PreferredMaterialNormalizer.normalize(material)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("소재가 없으면 null을 반환한다")
    void normalize_material_returns_null_when_empty(String material) {
        assertThat(PreferredMaterialNormalizer.normalize(material)).isNull();
    }

    @ParameterizedTest
    @CsvSource({"'울 100%'", "'면'", "'알 수 없음'"})
    @DisplayName("선택지에 없는 소재 원문은 노출하지 않는다")
    void normalize_material_returns_null_when_unsupported(String material) {
        assertThat(PreferredMaterialNormalizer.normalize(material)).isNull();
    }
}

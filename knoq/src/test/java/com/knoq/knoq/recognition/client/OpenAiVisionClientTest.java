package com.knoq.knoq.recognition.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.knoq.knoq.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiVisionClientTest {

    private OpenAiVisionClient client;
    private Product product;

    @BeforeEach
    void setUp() {
        client = new OpenAiVisionClient();
        ReflectionTestUtils.setField(client, "visionModel", "gpt-4o-mini");

        product = Product.of(
                "prod_1", "PD-1", "테스트 가방", "비세토스", "특징", 100_000L,
                List.of("L"), List.of("Cognac"), null, null, null
        );
        product.addReferenceImage("iVBOR-reference-front");
        product.addReferenceImage("iVBOR-reference-side");
    }

    @Test
    @DisplayName("1차 선별은 제품별 대표 사진 1장과 촬영 사진만 low로 보낸다")
    void firstPass_usesOneReferenceImageWithLowDetail() {
        ObjectNode body = client.buildRequestBody(
                "captured-jpeg", List.of(product), "low", true);

        List<JsonNode> imageParts = imageParts(body);
        assertThat(imageParts).hasSize(2);
        assertThat(imageParts)
                .allSatisfy(part -> assertThat(part.path("image_url").path("detail").asText())
                        .isEqualTo("low"));
    }

    @Test
    @DisplayName("2차 정밀 비교는 후보의 모든 기준 사진과 촬영 사진을 high로 보낸다")
    void secondPass_usesAllReferenceImagesWithHighDetail() {
        ObjectNode body = client.buildRequestBody(
                "captured-jpeg", List.of(product), "high", false);

        List<JsonNode> imageParts = imageParts(body);
        assertThat(imageParts).hasSize(3);
        assertThat(imageParts)
                .allSatisfy(part -> assertThat(part.path("image_url").path("detail").asText())
                        .isEqualTo("high"));
    }

    @Test
    @DisplayName("기준 사진의 Base64 시그니처로 이미지 형식을 구분한다")
    void detectsImageMediaType() {
        assertThat(client.detectMediaType("iVBORw0KGgo")).isEqualTo("image/png");
        assertThat(client.detectMediaType("UklGR-test")).isEqualTo("image/webp");
        assertThat(client.detectMediaType("/9j/jpeg")).isEqualTo("image/jpeg");
    }

    private List<JsonNode> imageParts(ObjectNode body) {
        JsonNode content = body.path("messages").get(0).path("content");
        return StreamSupport.stream(content.spliterator(), false)
                .filter(part -> "image_url".equals(part.path("type").asText()))
                .toList();
    }
}

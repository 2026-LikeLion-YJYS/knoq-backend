package com.knoq.knoq.recommendation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.knoq.knoq.product.entity.Product;
import com.knoq.knoq.recommendation.dto.response.FitAnalysisResponse;
import com.knoq.knoq.sessions.entity.LifestyleTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class FitAnalysisGenerator {

    private static final Logger log = LoggerFactory.getLogger(FitAnalysisGenerator.class);
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${knoq.openai.api-key}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FitAnalysisResponse generate(List<LifestyleTag> lifestyleTags, Product product) {
        try {
            String requestBody = objectMapper.writeValueAsString(buildRequestBody(lifestyleTags, product));
            String rawResponse = restClient.post()
                    .uri(API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseResponse(rawResponse);
        } catch (Exception e) {
            log.warn("제품 적합 분석 생성 실패, 규칙 기반 문장으로 대체합니다.", e);
            return null;
        }
    }

    private ObjectNode buildRequestBody(List<LifestyleTag> lifestyleTags, Product product) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", "gpt-4o-mini");
        root.put("max_tokens", 400);

        ObjectNode responseFormat = objectMapper.createObjectNode();
        responseFormat.put("type", "json_object");
        root.set("response_format", responseFormat);

        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content",
                "너는 매장 제품 적합 분석 어시스턴트다. " +
                        "제공된 라이프스타일 태그와 제품 속성만 비교해 한국어 존댓말로 설명해라. " +
                        "추측, 구매 강요, 제공되지 않은 사실은 포함하지 마라. " +
                        "반드시 JSON 객체만 반환하고 필드는 summary 문자열, reasons 문자열 배열, cautions 문자열 배열만 사용해라."
        );
        messages.add(systemMessage);

        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", buildPrompt(lifestyleTags, product));
        messages.add(userMessage);
        root.set("messages", messages);
        return root;
    }

    private String buildPrompt(List<LifestyleTag> lifestyleTags, Product product) {
        return "고객 라이프스타일 태그: " + lifestyleTags + "\n" +
                "제품명: " + value(product.getName()) + "\n" +
                "카테고리: " + value(product.getCategory()) + "\n" +
                "소재: " + value(product.getMaterial()) + "\n" +
                "특징: " + value(product.getFeatures()) + "\n" +
                "색상: " + product.getColors() + "\n" +
                "사이즈: " + product.getSizes() + "\n" +
                "브랜드 설명: " + value(product.getBrandOfficialDescription()) + "\n" +
                "AI 제품 설명: " + value(product.getAiGeneratedDescription()) + "\n" +
                "위 정보만 근거로 핵심 요약 1문장, 적합 이유, 주의점을 작성해줘.";
    }

    private FitAnalysisResponse parseResponse(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return null;
        }

        String content = choices.get(0).path("message").path("content").asText();
        if (content == null || content.isBlank()) {
            return null;
        }

        JsonNode result = objectMapper.readTree(content);
        String summary = result.path("summary").asText();
        List<String> reasons = stringList(result.path("reasons"));
        List<String> cautions = stringList(result.path("cautions"));
        if (summary.isBlank() || reasons.isEmpty()) {
            return null;
        }

        return new FitAnalysisResponse(summary, reasons, cautions);
    }

    private List<String> stringList(JsonNode arrayNode) {
        List<String> values = new ArrayList<>();
        if (!arrayNode.isArray()) {
            return values;
        }
        for (JsonNode node : arrayNode) {
            String value = node.asText();
            if (!value.isBlank()) {
                values.add(value);
            }
        }
        return values;
    }

    private String value(String value) {
        return value == null || value.isBlank() ? "정보 없음" : value;
    }
}

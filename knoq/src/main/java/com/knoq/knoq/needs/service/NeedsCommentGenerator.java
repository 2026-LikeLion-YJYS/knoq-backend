package com.knoq.knoq.needs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NeedsCommentGenerator {

    private static final Logger log = LoggerFactory.getLogger(NeedsCommentGenerator.class);
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${knoq.openai.api-key}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generate(String category, String color, String material, String size) {
        try {
            String requestBodyJson = objectMapper.writeValueAsString(
                    buildRequestBody(category, color, material, size)
            );

            String rawResponse = restClient.post()
                    .uri(API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBodyJson)
                    .retrieve()
                    .body(String.class);

            return parseComment(rawResponse);
        } catch (Exception e) {
            log.warn("니즈 분석 코멘트 생성 실패, 룰 기반 문장으로 대체합니다.", e);
            return null;
        }
    }

    private ObjectNode buildRequestBody(String category, String color, String material, String size) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", "gpt-4o-mini");
        root.put("max_tokens", 150);

        ArrayNode messages = objectMapper.createArrayNode();

        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content",
                "너는 매장 니즈 분석 코멘트를 작성하는 어시스턴트다. " +
                        "주어진 속성 값만 근거로 한국어 존댓말 1문장으로 간결하게 작성해라. " +
                        "제공되지 않은 정보는 언급하지 말고, 과장하거나 새로운 사실을 추가하지 마라."
        );
        messages.add(systemMessage);

        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", buildPrompt(category, color, material, size));
        messages.add(userMessage);

        root.set("messages", messages);
        return root;
    }

    private String buildPrompt(String category, String color, String material, String size) {
        StringBuilder sb = new StringBuilder("고객이 저장한 제품들을 집계한 결과는 다음과 같다.\n");
        if (category != null) sb.append("- 선호 카테고리: ").append(category).append("\n");
        if (material != null) sb.append("- 선호 소재: ").append(material).append("\n");
        if (color != null) sb.append("- 선호 색상: ").append(color).append("\n");
        if (size != null) sb.append("- 선호 사이즈: ").append(size).append("\n");
        sb.append("이 정보를 바탕으로 고객의 니즈를 한 문장으로 자연스럽게 설명해줘.");
        return sb.toString();
    }

    private String parseComment(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);
        String content = root.path("choices").get(0).path("message").path("content").asText();
        return (content == null || content.isBlank()) ? null : content.trim();
    }
}

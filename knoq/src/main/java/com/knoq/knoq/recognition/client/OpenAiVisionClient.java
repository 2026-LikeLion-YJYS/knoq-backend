package com.knoq.knoq.recognition.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.product.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI GPT-4o-mini 비전 기능으로 촬영 사진과 등록된 제품 기준 사진들을 비교해서
 * 가장 일치하는 제품(들)을 confidence와 함께 받아옴.
 * 별도 임베딩 저장 없이, 인식할 때마다 기준 사진들 + 촬영 사진을 한 번에 GPT한테 보냄.
 */
@Component
public class OpenAiVisionClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiVisionClient.class);
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${knoq.openai.api-key}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public record VisionMatch(String productId, double confidence) {}

    public List<VisionMatch> recognize(String capturedImageBase64, List<Product> referenceProducts) {
        try {
            ObjectNode requestBody = buildRequestBody(capturedImageBase64, referenceProducts);
            // Spring 쪽 기본 JSON 컨버터(Jackson 3, tools.jackson)가 우리가 만든 구버전 Jackson(com.fasterxml)
            // ObjectNode를 제대로 직렬화 못 해서 빈 바디가 나가는 문제가 있었음.
            // 그래서 우리 ObjectMapper로 직접 문자열로 변환해서 보내는 걸로 우회함 (버전 충돌 회피)
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            String rawResponse = restClient.post()
                    .uri(API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBodyJson)
                    .retrieve()
                    .body(String.class);

            log.info("OpenAI 인식 응답 원문: {}", rawResponse);

            return parseMatches(rawResponse);
        } catch (Exception e) {
            log.error("OpenAI 비전 인식 실패", e);
            throw new ApiException(ErrorCode.VISION_RECOGNITION_FAILED);
        }
    }

    private ObjectNode buildRequestBody(String capturedImageBase64, List<Product> referenceProducts) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", "gpt-4o-mini");

        ObjectNode responseFormat = objectMapper.createObjectNode();
        responseFormat.put("type", "json_object");
        root.set("response_format", responseFormat);

        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");

        ArrayNode content = objectMapper.createArrayNode();
        content.add(textPart("아래는 매장에 있는 제품들의 참고 사진입니다. 각 사진 앞에 productId를 표시했습니다."));

        for (Product product : referenceProducts) {
            content.add(textPart("productId: " + product.getId()));
            for (String referenceImage : product.getReferenceImages()) {
                content.add(imagePart(referenceImage));
            }
        }

        content.add(textPart("다음은 고객이 방금 촬영한 사진입니다. 위 참고 사진들과 비교해서 가장 일치하는 제품을 찾아주세요."));
        content.add(imagePart(capturedImageBase64));
        content.add(textPart(
                "결과를 JSON으로만 응답하세요. 형식: {\"matches\": [{\"productId\": \"prod_1\", \"confidence\": 0.93}]} " +
                        "confidence는 0~1 사이 값이고, 확신도 높은 순으로 최대 3개까지 정렬해서 반환하세요."
        ));

        userMessage.set("content", content);
        messages.add(userMessage);
        root.set("messages", messages);
        return root;
    }

    private ObjectNode textPart(String text) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", "text");
        node.put("text", text);
        return node;
    }

    private ObjectNode imagePart(String base64) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", "image_url");
        ObjectNode imageUrl = objectMapper.createObjectNode();
        imageUrl.put("url", "data:image/jpeg;base64," + base64);
        // low detail = 512x512로 축소해서 봄, 이미지당 고정 ~85토큰만 씀 (원본 그대로 보내면 타일링 때문에
        // 이미지 한 장에 수천 토큰까지 나가서 TPM 레이트리밋에 바로 걸림 — 실제로 요청 1번에 48만 토큰 나갔었음)
        imageUrl.put("detail", "low");
        node.set("image_url", imageUrl);
        return node;
    }

    private List<VisionMatch> parseMatches(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);
        String content = root.path("choices").get(0).path("message").path("content").asText();
        JsonNode parsedContent = objectMapper.readTree(content);

        List<VisionMatch> matches = new ArrayList<>();
        for (JsonNode match : parsedContent.path("matches")) {
            matches.add(new VisionMatch(match.path("productId").asText(), match.path("confidence").asDouble()));
        }
        return matches;
    }
}
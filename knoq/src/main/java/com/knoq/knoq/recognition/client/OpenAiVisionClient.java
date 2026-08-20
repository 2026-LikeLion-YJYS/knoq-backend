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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Value("${knoq.openai.vision-model:gpt-4o-mini}")
    private String visionModel;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public record VisionMatch(String productId, double confidence) {}

    public List<VisionMatch> recognize(String capturedImageBase64, List<Product> referenceProducts) {
        try {
            // 1차는 제품별 대표 이미지 1장을 저해상도로 비교해 후보를 줄인다.
            List<VisionMatch> firstPassMatches = requestMatches(
                    capturedImageBase64, referenceProducts, "low", true, "1차 후보 선별");

            Map<String, Product> productById = referenceProducts.stream()
                    .collect(Collectors.toMap(Product::getId, Function.identity()));
            List<Product> shortlistedProducts = firstPassMatches.stream()
                    .filter(match -> productById.containsKey(match.productId()))
                    .sorted(Comparator.comparingDouble(VisionMatch::confidence).reversed())
                    .map(match -> productById.get(match.productId()))
                    .distinct()
                    .limit(3)
                    .toList();

            if (shortlistedProducts.isEmpty()) {
                return List.of();
            }

            // 2차는 후보 3개의 모든 기준 이미지를 high로 보고 미세한 차이를 비교한다.
            try {
                return requestMatches(
                        capturedImageBase64, shortlistedProducts, "high", false, "2차 정밀 비교");
            } catch (Exception secondPassError) {
                // high 요청이 레이트 리밋 등으로 실패해도 인식 전체를 실패시키지 않고
                // 랜덤이 아닌 1차 AI 후보를 그대로 반환한다.
                log.warn("OpenAI 2차 정밀 비교 실패, 1차 AI 후보로 대체합니다.", secondPassError);
                return firstPassMatches.stream()
                        .filter(match -> productById.containsKey(match.productId()))
                        .sorted(Comparator.comparingDouble(VisionMatch::confidence).reversed())
                        .limit(3)
                        .toList();
            }
        } catch (Exception e) {
            log.error("OpenAI 비전 인식 실패", e);
            throw new ApiException(ErrorCode.VISION_RECOGNITION_FAILED);
        }
    }

    private List<VisionMatch> requestMatches(
            String capturedImageBase64,
            List<Product> referenceProducts,
            String detail,
            boolean representativeOnly,
            String stage
    ) throws Exception {
        ObjectNode requestBody = buildRequestBody(
                capturedImageBase64, referenceProducts, detail, representativeOnly);
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

        log.info("OpenAI 인식 응답 원문 ({}): {}", stage, rawResponse);

        return parseMatches(rawResponse);
    }

    ObjectNode buildRequestBody(
            String capturedImageBase64,
            List<Product> referenceProducts,
            String detail,
            boolean representativeOnly
    ) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", visionModel);

        ObjectNode responseFormat = objectMapper.createObjectNode();
        responseFormat.put("type", "json_object");
        root.set("response_format", responseFormat);

        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");

        ArrayNode content = objectMapper.createArrayNode();
        content.add(textPart("""
                아래는 매장 제품의 기준 사진입니다.
                촬영 각도와 조명, 배경, 크기 차이는 무시하고 제품 자체를 비교하세요.
                실루엣, 핸들 개수와 위치, 스트랩, 잠금장치, 포켓, 지퍼, 로고 패턴, 장식을 우선 비교하세요.
                반드시 아래에 제공된 productId만 반환하세요.
                """));

        for (Product product : referenceProducts) {
            content.add(textPart(String.format(
                    "productId: %s | name: %s | material: %s | colors: %s",
                    product.getId(), product.getName(), product.getMaterial(), product.getColors())));
            List<String> images = product.getReferenceImages();
            int imageCount = representativeOnly ? Math.min(1, images.size()) : images.size();
            for (int i = 0; i < imageCount; i++) {
                content.add(imagePart(images.get(i), detail));
            }
        }

        content.add(textPart("""
                다음은 고객이 방금 촬영한 사진입니다.
                제품이 화면의 일부분만 차지해도 제품 영역을 중심으로 확대해 형태와 장식을 비교하세요.
                위 참고 사진들과 비교해서 가장 일치하는 제품을 찾아주세요.
                """));
        // 1차 선별에서 기준 사진은 low로 절약하더라도, 작게 찍힌 제품의
        // 로고·포켓·스트랩을 놓치지 않도록 고객 촬영본은 항상 high로 보낸다.
        content.add(imagePart(capturedImageBase64, "high"));
        content.add(textPart(
                "결과를 JSON으로만 응답하세요. 형식: {\"matches\": [{\"productId\": \"prod_1\", \"confidence\": 0.93}]} " +
                        "confidence는 시각적 일치도를 나타내는 0~1 사이 값입니다. " +
                        "확신도가 높은 순으로 서로 다른 productId를 최대 3개까지 반환하세요. " +
                        "시각적 근거가 약하면 임의로 높은 confidence를 주지 마세요."
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

    private ObjectNode imagePart(String base64, String detail) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", "image_url");
        ObjectNode imageUrl = objectMapper.createObjectNode();
        imageUrl.put("url", "data:" + detectMediaType(base64) + ";base64," + base64);
        imageUrl.put("detail", detail);
        node.set("image_url", imageUrl);
        return node;
    }

    String detectMediaType(String base64) {
        if (base64.startsWith("iVBOR")) return "image/png";
        if (base64.startsWith("UklGR")) return "image/webp";
        if (base64.startsWith("R0lG")) return "image/gif";
        return "image/jpeg";
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

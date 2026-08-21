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

            log.info("OpenAI 제품 적합 분석 응답 원문: {}", rawResponse);
            return parseResponse(rawResponse);
        } catch (Exception e) {
            log.warn("제품 적합 분석 생성 실패, 규칙 기반 문장으로 대체합니다.", e);
            return null;
        }
    }

    private ObjectNode buildRequestBody(List<LifestyleTag> lifestyleTags, Product product) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", "gpt-4o-mini");
        root.put("max_tokens", 180);
        root.put("temperature", 0.2);

        ObjectNode responseFormat = objectMapper.createObjectNode();
        responseFormat.put("type", "json_object");
        root.set("response_format", responseFormat);

        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", """
                너는 럭셔리 패션 쇼핑 서비스 KNOQ의 라이프스타일 적합 분석 어시스턴트다.
                제공된 라이프스타일 태그와 제품 속성만 비교해 한국어 존댓말로 설명해라.
                추측, 구매 강요, 제공되지 않은 사실은 포함하지 마라.

                총 3문장만 생성하고 다음 순서를 반드시 지켜라.
                1. summary: 제품과 가장 잘 맞는 라이프스타일 태그 1개와 실제 디자인 속성 1개를 연결한 1문장
                2. reasons[0]: 입력에 명시된 사이즈·소재·구성·기능 중 구체적 장점 1문장
                3. reasons[1]: 입력의 용도 또는 제품 설명에 명시된 사용 상황 1문장

                작성 예시:
                - 평소 선호하는 미니멀 스타일과 잘 어울립니다.
                - 노트북 수납이 가능한 사이즈입니다.
                - 출퇴근용으로 적합합니다.

                각 문장은 45자 이내의 자연스러운 존댓말로 작성하라.
                여러 라이프스타일 태그를 '하면서도', '이면서' 등으로 한 문장에 나열하지 마라.
                제품과 가장 관련성이 높은 태그 1개만 선택하라.
                세 문장은 각각 입력에 실제로 적힌 서로 다른 제품 속성을 근거로 삼아라.
                입력에 없는 '가벼운', '부드러운', '편안한 착용감', '내구성', '수납력'을 소재나 크기만 보고 추론하지 마라.
                노트북 수납, 출퇴근용 등도 입력에 명시된 경우에만 사용하라.
                구체적 기능이나 사용 상황이 없으면 이를 만들지 말고 입력된 색상·소재·크기를 객관적으로 설명하라.
                제품명과 속성이 다른데도 다른 제품에 그대로 사용할 수 있는 범용적인 문장만 작성하지 마라.
                세 문장에서 같은 표현이나 근거를 반복하지 마라.
                cautions는 반드시 빈 배열로 반환하라.

                반드시 JSON 객체만 반환하고 summary 문자열, reasons 문자열 배열,
                cautions 문자열 배열 외의 필드는 사용하지 마라.
                """);
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
                "스타일 속성: " + product.getFeatureStyles() + "\n" +
                "구성 속성: " + product.getFeatureCompositions() + "\n" +
                "용도 속성: " + product.getFeatureUsages() + "\n" +
                "색상: " + product.getColors() + "\n" +
                "사이즈: " + product.getSizes() + "\n" +
                "브랜드 설명: " + value(product.getBrandOfficialDescription()) + "\n" +
                "AI 제품 설명: " + value(product.getAiGeneratedDescription()) + "\n" +
                "위 정보만 근거로 summary 1문장과 reasons 2문장을 작성해줘. cautions는 빈 배열로 반환해줘.";
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
        if (summary.isBlank() || reasons.size() < 2) {
            return null;
        }

        return new FitAnalysisResponse(summary, reasons.subList(0, 2), List.of());
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

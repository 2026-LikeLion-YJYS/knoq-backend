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

import java.util.List;

@Component
public class NeedsCommentGenerator {

    private static final Logger log = LoggerFactory.getLogger(NeedsCommentGenerator.class);
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${knoq.openai.api-key}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generate(List<ProductAttributes> products) {
        try {
            String requestBodyJson = objectMapper.writeValueAsString(
                    buildRequestBody(products)
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

    private ObjectNode buildRequestBody(List<ProductAttributes> products) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", "gpt-4o-mini");
        root.put("max_tokens", 500);

        ObjectNode responseFormat = objectMapper.createObjectNode();
        responseFormat.put("type", "json_object");
        root.set("response_format", responseFormat);

        ArrayNode messages = objectMapper.createArrayNode();

        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT);
        messages.add(systemMessage);

        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", buildPrompt(products));
        messages.add(userMessage);

        root.set("messages", messages);
        return root;
    }

    private String buildPrompt(List<ProductAttributes> products) throws Exception {
        ObjectNode input = objectMapper.createObjectNode();
        input.set("products", objectMapper.valueToTree(products));
        return "다음은 고객이 저장한 전체 제품 데이터다. 입력된 데이터만 사용해 분석해라.\n"
                + objectMapper.writeValueAsString(input);
    }

    private String parseComment(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);
        String content = root.path("choices").get(0).path("message").path("content").asText();
        if (content == null || content.isBlank()) {
            return null;
        }

        JsonNode insight = objectMapper.readTree(content);
        String coreNeed = insight.path("coreNeed").asText().trim();
        String comment = insight.path("comment").asText().trim();
        JsonNode evidence = insight.path("evidence");
        String confidence = insight.path("confidence").asText().trim();

        if (coreNeed.isBlank()
                || comment.isBlank()
                || !evidence.isArray()
                || evidence.size() > 3
                || !List.of("HIGH", "MEDIUM", "LOW").contains(confidence)) {
            return null;
        }
        return comment;
    }

    private static final String SYSTEM_PROMPT = """
            너는 럭셔리 패션 쇼핑 서비스 KNOQ의 AI 니즈 분석 엔진이다.

            고객이 저장한 여러 제품을 서로 비교하여 반복되는 공통 속성을 찾고,
            이를 실제 쇼핑에 활용할 수 있는 하나의 핵심 구매 니즈로 해석한다.

            분석 규칙:
            1. 개별 제품의 특징을 단순 나열하지 않는다.
            2. 모든 제품의 속성을 비교하고 최소 2개 제품에서 반복되는 공통점을 우선한다.
            3. 단순 최빈 단어를 그대로 니즈로 선정하지 않는다.
            4. 서로 다른 속성이 하나의 구매 이유로 연결되면 상위 구매 기준으로 통합한다.
            5. 분석 과정은 '제품 속성 -> 반복되는 공통점 -> 상위 구매 기준 -> 핵심 니즈' 순서로 진행한다.
            6. 색상이나 브랜드만 반복된다는 이유로 핵심 니즈로 과도하게 해석하지 않는다.
            7. 제품 수가 적거나 공통점이 약하면 근거 없는 니즈를 만들지 말고 confidence를 낮춘다.
            8. 입력에 없는 제품 정보, 사용자 성향, 직업, 소득, 성별, 구매 의도를 추측하지 않는다.
            9. 핵심 니즈는 구체적인 구매 기준 하나만 선정한다.
            10. 핵심 니즈의 주요 근거는 최대 3개까지만 선택한다.

            좋은 핵심 니즈 예시:
            - 실용성과 활용도
            - 클래식하고 오래 사용할 수 있는 디자인
            - 수납성과 편의성
            - 미니멀한 디자인
            - 개성 있는 디자인
            - 소재의 고급스러움
            - 다양한 스타일에 활용할 수 있는 제품

            comment 작성 규칙:
            - 사용자에게 직접 말하는 자연스러운 한국어 존댓말 한 문장으로 작성한다.
            - 기술적인 분석 보고서 표현을 사용하지 않는다.
            - 속성들을 쉼표로 나열하지 말고, 핵심 구매 니즈를 중심으로 설명한다.
            - 예: "고객님은 실용성과 활용도를 중요하게 생각하고 있어요."

            반드시 아래 형식의 JSON 객체만 반환한다:
            {
              "coreNeed": "핵심 구매 니즈 1개",
              "comment": "사용자에게 보여줄 자연스러운 문장",
              "evidence": [
                {
                  "attributes": ["입력에서 확인한 주요 속성"],
                  "purchaseCriterion": "속성에서 도출한 상위 구매 기준"
                }
              ],
              "confidence": "HIGH 또는 MEDIUM 또는 LOW"
            }
            """;
}

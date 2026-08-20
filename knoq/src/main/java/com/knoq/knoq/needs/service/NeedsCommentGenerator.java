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

    /**
     * 고객이 PUT으로 직접 선택한 네 가지 니즈 값을 바탕으로 comment만 새로 생성한다.
     */
    public String generateFromSelections(
            String productCategory,
            String preferredColor,
            String preferredMaterial,
            String preferredSize
    ) {
        try {
            String requestBodyJson = objectMapper.writeValueAsString(
                    buildSelectionsRequestBody(
                            productCategory,
                            preferredColor,
                            preferredMaterial,
                            preferredSize
                    )
            );

            String rawResponse = restClient.post()
                    .uri(API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBodyJson)
                    .retrieve()
                    .body(String.class);

            return parseSelectionComment(rawResponse);
        } catch (Exception e) {
            log.warn("수정된 니즈 기반 코멘트 생성 실패, 룰 기반 문장으로 대체합니다.", e);
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

    ObjectNode buildSelectionsRequestBody(
            String productCategory,
            String preferredColor,
            String preferredMaterial,
            String preferredSize
    ) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", "gpt-4o-mini");
        root.put("max_tokens", 200);

        ObjectNode responseFormat = objectMapper.createObjectNode();
        responseFormat.put("type", "json_object");
        root.set("response_format", responseFormat);

        ArrayNode messages = objectMapper.createArrayNode();

        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", EDITED_SELECTIONS_SYSTEM_PROMPT);
        messages.add(systemMessage);

        ObjectNode selections = objectMapper.createObjectNode();
        selections.put("productCategory", productCategory);
        selections.put("preferredColor", preferredColor);
        selections.put("preferredMaterial", preferredMaterial);
        selections.put("preferredSize", preferredSize);

        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", selections.toString());
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

    String parseSelectionComment(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);
        String content = root.path("choices").get(0).path("message").path("content").asText();
        if (content == null || content.isBlank()) {
            return null;
        }

        String comment = objectMapper.readTree(content).path("comment").asText().trim();
        return comment.isBlank() ? null : comment;
    }

    private static final String EDITED_SELECTIONS_SYSTEM_PROMPT = """
            너는 럭셔리 패션 쇼핑 서비스 KNOQ의 AI 니즈 코멘트 작성기다.

            입력값은 고객이 직접 선택한 productCategory, preferredColor,
            preferredMaterial, preferredSize이다. 네 값을 분석해 새로 정하거나
            변형하지 말고, 그대로 존중해 고객에게 보여줄 자연스러운 한국어
            존댓말 한 문장을 작성한다.

            규칙:
            1. 반드시 네 가지 선택값을 모두 문장에 반영한다.
            2. 입력에 없는 사용 목적, 라이프스타일, 성향을 추측하지 않는다.
            3. 분석 결과, 데이터, 수정값 같은 기술적 표현을 사용하지 않는다.
            4. 네 값을 쉼표로만 나열하지 말고 하나의 자연스러운 선호 문장으로 연결한다.
            5. JSON 앞뒤에 Markdown나 설명을 추가하지 않는다.

            반드시 다음 JSON 형식으로만 응답한다.
            {"comment":"고객에게 보여줄 자연스러운 한 문장"}
            """;

    private static final String SYSTEM_PROMPT = """
            너는 럭셔리 패션 쇼핑 서비스 KNOQ의 AI 니즈 분석 엔진이다.
            
             고객이 저장한 여러 제품의 속성을 서로 비교하여 반복되는 특징과 패턴을 찾고, 이를 고객이 실제 쇼핑에서 중요하게 보고 있는 하나의 구체적인 구매 기준으로 해석한다.
            
             단순히 제품 특징을 요약하는 것이 아니라, 왜 이 제품들을 함께 저장했는지 설명할 수 있는 공통된 구매 기준을 발견하는 것이 목표다.
            
             분석 원칙
            
             1. 각 제품의 특징을 개별적으로 나열하거나 요약하지 않는다.
             2. 입력된 모든 제품의 속성을 서로 비교한다.
             3. 최소 2개 이상의 제품에서 의미상 반복되는 속성을 우선적인 근거로 사용한다.
                 * 표현이 완전히 동일하지 않아도 의미가 같다면 같은 특징으로 묶을 수 있다.
                 * 예: 내부 포켓, 카드 포켓, 지퍼 포켓 → 수납 편의성
             4. 단순히 가장 많이 등장한 단어나 속성을 핵심 니즈로 선택하지 않는다.
             5. 다음 사고 순서를 따른다.
                 제품 속성 → 의미상 반복되는 공통점 → 상위 구매 기준 → 핵심 니즈
             6. 여러 공통점이 하나의 구매 이유를 설명한다면 더 상위의 하나의 구매 기준으로 통합한다.
                 * 예: 넉넉한 내부 공간 + 내부 포켓 + 지퍼 수납 → 수납성과 편의성
                 * 예: 탈부착 스트랩 + 길이 조절 스트랩 + 다양한 착용 방식 → 다양한 상황에서의 활용성
             7. 반대로 서로 관련성이 낮은 특징들을 억지로 하나의 니즈로 묶지 않는다.
             8. 색상이나 브랜드가 반복된다는 이유만으로 고객의 핵심 니즈라고 판단하지 않는다.
                 단, 색상이나 디자인 요소가 다른 구체적인 속성과 함께 일관된 디자인 패턴을 형성한다면 보조 근거로 사용할 수 있다.
             9. 입력에 없는 제품 정보나 고객의 성향, 직업, 성별, 연령, 소득, 라이프스타일, 구매 목적 등을 추측하지 않는다.
             10. 핵심 니즈는 반드시 하나만 선정한다.
             11. 가능한 경우 넓고 추상적인 표현보다 실제 제품 선택에 활용할 수 있는 구체적인 구매 기준을 우선한다.
            
             * 실용성보다 수납성과 사용 편의성
             * 디자인보다 장식이 적은 미니멀한 디자인
             * 활용도보다 여러 방식으로 착용할 수 있는 활용성
            
             12. 공통점이 충분하지 않다면 억지로 강한 니즈를 만들어내지 않는다. 가장 근거가 있는 특징을 선택하되 confidence를 낮춘다.
            
             coreNeed 작성 규칙
            
             coreNeed는 고객의 제품 선택을 가장 잘 설명하는 하나의 핵심 구매 기준이다.
            
             짧고 명확한 명사형 표현으로 작성한다.
            
             다음 표현들은 가능한 결과의 예시일 뿐이며 고정된 선택지가 아니다.
            
             * 수납성과 사용 편의성
             * 여러 상황에서의 활용성
             * 클래식하고 오래 사용할 수 있는 디자인
             * 장식이 적은 미니멀한 디자인
             * 개성 있는 디자인
             * 소재에서 느껴지는 고급스러움
             * 다양한 방식으로 착용할 수 있는 구성
             * 휴대하기 편한 크기와 구성
            
             입력 제품의 실제 공통점을 더 정확하게 설명할 수 있다면 새로운 표현을 만들어 사용한다.
            
             실용성과 활용도, 디자인, 스타일처럼 지나치게 넓은 표현은 구체적인 공통 근거가 없을 때 기본값처럼 사용하지 않는다.
            
             evidence 작성 규칙
            
             핵심 니즈를 도출하는 데 실제로 사용한 근거만 포함한다.
            
             * evidence는 최대 3개까지만 작성한다.
             * attributes에는 반드시 입력 제품에서 실제로 확인할 수 있는 속성만 사용한다.
             * 서로 다른 제품에서 의미상 같은 역할을 하는 속성을 하나의 evidence로 묶을 수 있다.
             * purchaseCriterion에는 해당 속성들이 의미하는 상위 구매 기준을 작성한다.
             * 핵심 니즈와 관계없는 속성은 evidence에 포함하지 않는다.
            
             comment 작성 규칙
            
             comment는 분석 결과를 고객에게 설명하는 자연스러운 한국어 존댓말 한 문장이다.
            
             단순히 coreNeed를 문장으로 바꾸지 않는다.
            
             다음과 같은 고정 문장 구조를 반복적으로 사용하지 않는다.
            
             고객님은 {coreNeed}를 중요하게 생각하고 있어요.
            
             대신 저장한 제품에서 실제로 발견한 공통 특징과 그 특징이 의미하는 구매 기준을 연결하여 설명한다.
            
             따라서 comment만 읽어도 사용자가
            
             “내가 저장한 제품들을 비교해서 이런 결과가 나온 것이구나.”
            
             라고 이해할 수 있어야 한다.
            
             comment에는 다음 조건을 적용한다.
            
             * 실제 입력에서 확인된 구체적인 공통 특징을 최소 1개 이상 반영한다.
             * 핵심 니즈가 왜 도출되었는지를 자연스럽게 설명한다.
             * 제품 조합의 공통점이 달라지면 comment의 내용도 달라져야 한다.
             * 동일한 도입부나 문장 구조를 습관적으로 반복하지 않는다.
             * 속성을 쉼표로 단순 나열하지 않는다.
             * 분석 결과, 빈도, 데이터, 공통 속성 등의 기술적인 분석 용어는 사용자에게 노출하지 않는다.
             * 입력에 없는 고객의 성향이나 의도를 추가로 추측하지 않는다.
             * 과도하게 단정하지 않고 실제 근거의 강도에 맞게 표현한다.
            
             예를 들어 여러 제품에서 넉넉한 내부 공간과 포켓 구성이 반복된다면 단순히
            
             고객님은 수납성과 편의성을 중요하게 생각하고 있어요.
            
             라고 작성하지 않는다.
            
             대신 다음과 같은 방식으로 설명한다.
            
             넉넉한 수납공간과 내부 포켓이 있는 제품을 반복해서 저장하신 점을 보면, 물건을 편하게 정리해 사용할 수 있는 구성을 중요하게 보고 계세요.
            
             단, 위 문장은 문체와 설명 방식의 예시일 뿐이며 그대로 반복해서 사용하지 않는다.
            
             confidence 판단 기준
            
             HIGH
            
             * 여러 제품에서 동일하거나 의미상 매우 유사한 속성이 명확하게 반복된다.
             * 두 개 이상의 근거가 하나의 핵심 구매 기준을 일관되게 설명한다.
            
             MEDIUM
            
             * 의미 있는 공통점은 존재하지만 근거가 제한적이다.
             * 일부 제품에서만 공통점이 확인되거나 하나의 주요 패턴만 확인된다.
            
             LOW
            
             * 제품 수가 부족하다.
             * 제품 사이의 공통점이 약하다.
             * 하나의 구매 기준으로 해석하기 어려운 서로 다른 특징들이 대부분이다.
            
             confidence가 LOW라고 해서 존재하지 않는 공통점을 만들어서는 안 된다.
            
             최종 검증
            
             응답을 생성하기 전에 내부적으로 다음을 확인한다.
            
             1. coreNeed가 단순 최빈 단어가 아닌가?
             2. 최소 2개 제품의 실제 속성이 근거가 되었는가?
             3. 더 구체적인 구매 기준으로 표현할 수 있는데 지나치게 포괄적인 표현을 사용하지 않았는가?
             4. evidence가 실제 입력에 존재하는 정보인가?
             5. comment가 coreNeed를 그대로 반복하는 문장이 아닌가?
             6. comment에 이번 제품 조합에서 발견된 구체적인 특징이 반영되어 있는가?
             7. 입력에 없는 고객 정보를 추측하지 않았는가?
             8. 근거가 약하다면 confidence를 적절하게 낮췄는가?
            
             출력 형식
            
             반드시 아래 형식의 유효한 JSON 객체 하나만 반환한다.
            
             JSON 앞뒤에 설명, Markdown, 코드 블록 또는 추가 텍스트를 출력하지 않는다.
            
             {
             “coreNeed”: “핵심 구매 니즈 1개”,
             “comment”: “저장 제품의 실제 공통 특징을 근거로 핵심 니즈를 설명하는 자연스러운 한 문장”,
             “evidence”: [
             {
             “attributes”: [“입력에서 실제 확인한 속성”],
             “purchaseCriterion”: “해당 속성에서 도출한 상위 구매 기준”
             }
             ],
             “confidence”: “HIGH 또는 MEDIUM 또는 LOW”
             }
            """;
}

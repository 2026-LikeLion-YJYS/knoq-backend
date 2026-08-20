package com.knoq.knoq.needs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NeedsCommentGeneratorTest {

    private final NeedsCommentGenerator generator = new NeedsCommentGenerator();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("PUT으로 수정한 네 가지 값을 모두 GPT 코멘트 요청에 포함한다")
    void selectionsRequestContainsAllUserEditedValues() throws Exception {
        ObjectNode request = generator.buildSelectionsRequestBody(
                "토트백 / 쇼퍼백",
                "Black · Cognac",
                "Leather",
                "Medium · Large"
        );

        String userContent = request.path("messages").get(1).path("content").asText();
        JsonNode selections = objectMapper.readTree(userContent);

        assertThat(selections.path("productCategory").asText()).isEqualTo("토트백 / 쇼퍼백");
        assertThat(selections.path("preferredColor").asText()).isEqualTo("Black · Cognac");
        assertThat(selections.path("preferredMaterial").asText()).isEqualTo("Leather");
        assertThat(selections.path("preferredSize").asText()).isEqualTo("Medium · Large");
    }

    @Test
    @DisplayName("GPT JSON 응답에서 comment만 추출한다")
    void parsesOnlyCommentFromSelectionResponse() throws Exception {
        String rawResponse = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "{\\\"comment\\\":\\\"수정된 니즈를 반영한 문장입니다.\\\"}"
                      }
                    }
                  ]
                }
                """;

        assertThat(generator.parseSelectionComment(rawResponse))
                .isEqualTo("수정된 니즈를 반영한 문장입니다.");
    }
}

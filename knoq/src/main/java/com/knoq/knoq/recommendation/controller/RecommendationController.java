package com.knoq.knoq.recommendation.controller;

import com.knoq.knoq.recommendation.dto.request.RecommendationRequest;
import com.knoq.knoq.recommendation.dto.response.RecommendationResponse;
import com.knoq.knoq.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions/{sessionId}/recommendations")
@Tag(name = "Recommendation", description = "라이프스타일 기반 제품 추천 API")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping
    @Operation(
            summary = "초기 제품 추천",
            description = "세션의 라이프스타일 태그를 기준으로 제품을 최대 3개 추천하고 보관함에 자동 저장합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "제품 추천 성공",
                    content = @Content(schema = @Schema(implementation = RecommendationResponse.class))),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
            @ApiResponse(responseCode = "410", description = "세션 만료")
    })
    public ResponseEntity<RecommendationResponse> recommend(
            @Parameter(example = "sess_abc123") @PathVariable String sessionId,
            @RequestBody RecommendationRequest request
    ) {
        return ResponseEntity.ok(recommendationService.recommend(sessionId));
    }
}

package com.knoq.knoq.recommendation.controller;

import com.knoq.knoq.recommendation.dto.response.FitAnalysisResponse;
import com.knoq.knoq.recommendation.service.FitAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions/{sessionId}/products/{productId}/fit-analysis")
@Tag(name = "Recommendation", description = "라이프스타일 기반 제품 추천 API")
public class FitAnalysisController {

    private final FitAnalysisService fitAnalysisService;

    @GetMapping
    @Operation(
            summary = "제품 적합 분석",
            description = "세션의 라이프스타일 태그와 제품 속성을 비교해 적합 이유와 주의점을 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "제품 적합 분석 성공",
                    content = @Content(schema = @Schema(implementation = FitAnalysisResponse.class))),
            @ApiResponse(responseCode = "404", description = "세션 또는 제품을 찾을 수 없음"),
            @ApiResponse(responseCode = "410", description = "세션 만료")
    })
    public ResponseEntity<FitAnalysisResponse> analyzeFit(
            @Parameter(example = "sess_abc123") @PathVariable String sessionId,
            @Parameter(example = "prod_1") @PathVariable String productId
    ) {
        return ResponseEntity.ok(fitAnalysisService.analyze(sessionId, productId));
    }
}

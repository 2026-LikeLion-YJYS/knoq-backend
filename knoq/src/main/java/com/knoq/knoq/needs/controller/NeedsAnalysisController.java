package com.knoq.knoq.needs.controller;

import com.knoq.knoq.needs.dto.request.UpdateNeedsAnalysisRequest;
import com.knoq.knoq.needs.dto.response.NeedsAnalysisResponse;
import com.knoq.knoq.needs.dto.response.NeedsAnalysisResultResponse;
import com.knoq.knoq.needs.service.NeedsAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions/{sessionId}/needs-analysis")
@Tag(name = "Needs Analysis", description = "니즈 분석 API")
public class NeedsAnalysisController {

    private final NeedsAnalysisService needsAnalysisService;

    @GetMapping
    @Operation(summary = "니즈 분석 조회", description = "분석 가능 여부, 저장 개수, 최근 분석 결과를 조회합니다.")
    public NeedsAnalysisResponse getAnalysis(
            @Parameter(example = "sess_abc123") @PathVariable String sessionId
    ) {
        return needsAnalysisService.getAnalysis(sessionId);
    }

    @PostMapping
    @Operation(summary = "니즈 분석 생성/재분석", description = "저장한 제품이 2개 이상이면 공통 속성을 집계해 분석합니다.")
    public NeedsAnalysisResultResponse analyze(
            @Parameter(example = "sess_abc123") @PathVariable String sessionId
    ) {
        return needsAnalysisService.analyze(sessionId);
    }

    @PutMapping
    @Operation(
            summary = "니즈 분석 결과 수정",
            description = "고객이 자동 분석된 네 가지 니즈 항목을 직접 수정합니다. 기존 분석 코멘트는 유지됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "니즈 분석 결과 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 수정 값"),
            @ApiResponse(responseCode = "404", description = "세션 또는 기존 니즈 분석을 찾을 수 없음"),
            @ApiResponse(responseCode = "410", description = "세션 만료")
    })
    public NeedsAnalysisResultResponse updateAnalysis(
            @Parameter(example = "sess_abc123") @PathVariable String sessionId,
            @Valid @RequestBody UpdateNeedsAnalysisRequest request
    ) {
        return needsAnalysisService.updateAnalysis(sessionId, request);
    }
}

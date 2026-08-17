package com.knoq.knoq.needs.controller;

import com.knoq.knoq.needs.dto.response.NeedsAnalysisResponse;
import com.knoq.knoq.needs.dto.response.NeedsAnalysisResultResponse;
import com.knoq.knoq.needs.service.NeedsAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
}
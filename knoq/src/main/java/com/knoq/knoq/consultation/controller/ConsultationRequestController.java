package com.knoq.knoq.consultation.controller;

import com.knoq.knoq.consultation.dto.request.CreateConsultationRequest;
import com.knoq.knoq.consultation.dto.response.CreateConsultationResponse;
import com.knoq.knoq.consultation.service.ConsultationRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions/{sessionId}/consultation-requests")
@Tag(name = "Consultation Request", description = "상담 요청 API")
public class ConsultationRequestController {

    private final ConsultationRequestService consultationRequestService;

    @PostMapping
    @Operation(summary = "상담 요청 생성", description = "도움 유형과 제품, 니즈 분석 포함 여부를 확정해 상담 요청을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "상담 요청 생성 성공",
                    content = @Content(schema = @Schema(implementation = CreateConsultationResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            @ApiResponse(responseCode = "404", description = "세션 또는 제품을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "활성 상담 요청 중복"),
            @ApiResponse(responseCode = "410", description = "세션 만료")
    })
    public ResponseEntity<CreateConsultationResponse> create(
            @Parameter(example = "sess_abc123") @PathVariable String sessionId,
            @Valid @RequestBody CreateConsultationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(consultationRequestService.create(sessionId, request));
    }
}

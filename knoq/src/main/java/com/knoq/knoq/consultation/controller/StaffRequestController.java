package com.knoq.knoq.consultation.controller;

import com.knoq.knoq.consultation.dto.request.UpdateConsultationStatusRequest;
import com.knoq.knoq.consultation.dto.response.StaffRequestDetailResponse;
import com.knoq.knoq.consultation.dto.response.StaffRequestInboxResponse;
import com.knoq.knoq.consultation.dto.response.UpdateConsultationStatusResponse;
import com.knoq.knoq.consultation.service.StaffRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/staff/requests")
@Tag(name = "Staff Request", description = "직원 상담 요청 조회 API")
@SecurityScheme(
        name = "staffBearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@SecurityRequirement(name = "staffBearerAuth")
public class StaffRequestController {

    private final StaffRequestService staffRequestService;

    @GetMapping
    @Operation(summary = "직원 요청 인박스 조회",
            description = "직원 토큰에 해당하는 매장의 상담 요청을 최신순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "직원 요청 인박스 조회 성공",
                    content = @Content(schema = @Schema(implementation = StaffRequestInboxResponse.class))),
            @ApiResponse(responseCode = "401", description = "직원 인증 실패")
    })
    public ResponseEntity<StaffRequestInboxResponse> findAll(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ResponseEntity.ok(staffRequestService.findAll(authorizationHeader));
    }

    @GetMapping("/{requestId}")
    @Operation(summary = "직원 요청 상세 조회",
            description = "직원 토큰에 해당하는 매장의 상담 요청 상세를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "직원 요청 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = StaffRequestDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "직원 인증 실패"),
            @ApiResponse(responseCode = "404", description = "상담 요청을 찾을 수 없음")
    })
    public ResponseEntity<StaffRequestDetailResponse> findDetail(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(example = "req_27") @PathVariable String requestId
    ) {
        return ResponseEntity.ok(staffRequestService.findDetail(authorizationHeader, requestId));
    }

    @PutMapping("/{requestId}/status")
    @Operation(summary = "상담 요청 상태 변경",
            description = "상담 요청 상태를 REQUESTED → ACCEPTED → IN_PROGRESS → COMPLETED 순서로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상담 요청 상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = UpdateConsultationStatusResponse.class))),
            @ApiResponse(responseCode = "400", description = "허용되지 않는 상태 전이"),
            @ApiResponse(responseCode = "401", description = "직원 인증 실패"),
            @ApiResponse(responseCode = "404", description = "상담 요청을 찾을 수 없음")
    })
    public ResponseEntity<UpdateConsultationStatusResponse> updateStatus(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(example = "req_27") @PathVariable String requestId,
            @Valid @RequestBody UpdateConsultationStatusRequest request
    ) {
        return ResponseEntity.ok(
                staffRequestService.updateStatus(authorizationHeader, requestId, request)
        );
    }
}

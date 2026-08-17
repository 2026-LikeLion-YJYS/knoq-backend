package com.knoq.knoq.consultation.dto.response;

import com.knoq.knoq.consultation.entity.ConsultationRequest;
import com.knoq.knoq.consultation.entity.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CreateConsultationResponse(
        @Schema(example = "req_27")
        String requestId,

        @Schema(example = "REQUESTED")
        RequestStatus status,

        LocalDateTime requestedAt
) {
    public static CreateConsultationResponse from(ConsultationRequest request) {
        return new CreateConsultationResponse(request.getId(), request.getStatus(), request.getRequestedAt());
    }
}

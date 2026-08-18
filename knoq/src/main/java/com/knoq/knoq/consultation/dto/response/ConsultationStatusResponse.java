package com.knoq.knoq.consultation.dto.response;

import com.knoq.knoq.consultation.entity.ConsultationRequest;
import com.knoq.knoq.consultation.entity.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ConsultationStatusResponse(
        @Schema(example = "req_27")
        String requestId,

        @Schema(example = "REQUESTED")
        RequestStatus status,

        LocalDateTime updatedAt
) {
    public static ConsultationStatusResponse from(ConsultationRequest request) {
        LocalDateTime updatedAt = request.getUpdatedAt() == null
                ? request.getRequestedAt()
                : request.getUpdatedAt();
        return new ConsultationStatusResponse(request.getId(), request.getStatus(), updatedAt);
    }
}

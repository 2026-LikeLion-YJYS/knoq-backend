package com.knoq.knoq.consultation.dto.response;

import com.knoq.knoq.consultation.entity.ConsultationRequest;
import com.knoq.knoq.consultation.entity.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record UpdateConsultationStatusResponse(
        @Schema(example = "req_27")
        String requestId,

        @Schema(example = "ACCEPTED")
        RequestStatus status,

        LocalDateTime updatedAt
) {
    public static UpdateConsultationStatusResponse from(ConsultationRequest request) {
        return new UpdateConsultationStatusResponse(
                request.getId(),
                request.getStatus(),
                request.getUpdatedAt()
        );
    }
}

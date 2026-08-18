package com.knoq.knoq.consultation.dto.request;

import com.knoq.knoq.consultation.entity.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateConsultationStatusRequest(
        @NotNull
        @Schema(example = "ACCEPTED")
        RequestStatus status
) {
}

package com.knoq.knoq.consultation.dto.response;

import com.knoq.knoq.consultation.entity.ConsultationRequest;
import com.knoq.knoq.consultation.entity.HelpType;
import com.knoq.knoq.consultation.entity.RequestStatus;
import com.knoq.knoq.sessions.entity.LifestyleTag;
import com.knoq.knoq.sessions.entity.Session;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record StaffRequestSummaryResponse(
        @Schema(example = "req_27")
        String requestId,

        @Schema(example = "노크고객")
        String nickname,

        @Schema(example = "PRODUCT_COMPARISON")
        HelpType helpType,

        List<LifestyleTag> lifestyleTags,

        @Schema(example = "2")
        int productCount,

        @Schema(example = "REQUESTED")
        RequestStatus status,

        LocalDateTime requestedAt
) {
    public static StaffRequestSummaryResponse of(ConsultationRequest request, Session session) {
        return new StaffRequestSummaryResponse(
                request.getId(),
                session.getNickname(),
                request.getHelpType(),
                List.copyOf(session.getLifestyleTags()),
                request.getProducts().size(),
                request.getStatus(),
                request.getRequestedAt()
        );
    }
}

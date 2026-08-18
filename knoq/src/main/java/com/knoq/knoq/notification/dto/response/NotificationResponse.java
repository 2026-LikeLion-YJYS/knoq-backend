package com.knoq.knoq.notification.dto.response;

import com.knoq.knoq.consultation.entity.RequestStatus;
import com.knoq.knoq.notification.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record NotificationResponse(
        @Schema(example = "ntf_1") String notificationId,
        @Schema(example = "req_27") String requestId,
        @Schema(example = "ACCEPTED") RequestStatus status,
        @Schema(example = "어드바이저가 확인했어요. 고객님이 살펴본 제품과 요청 내용을 확인하고 있습니다.") String message,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRequestId(),
                notification.getStatus(),
                notification.getMessage(),
                notification.getCreatedAt()
        );
    }
}

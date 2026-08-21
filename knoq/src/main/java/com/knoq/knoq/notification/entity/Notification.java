package com.knoq.knoq.notification.entity;

import com.knoq.knoq.consultation.entity.RequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Notification(
            String id,
            String sessionId,
            String requestId,
            RequestStatus status,
            String message
    ) {
        this.id = id;
        this.sessionId = sessionId;
        this.requestId = requestId;
        this.status = status;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public static Notification of(
            String id,
            String sessionId,
            String requestId,
            RequestStatus status,
            String message
    ) {
        return new Notification(id, sessionId, requestId, status, message);
    }
}

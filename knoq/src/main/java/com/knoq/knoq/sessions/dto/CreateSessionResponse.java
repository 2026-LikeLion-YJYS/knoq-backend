package com.knoq.knoq.sessions.dto;

import java.time.LocalDateTime;

public record CreateSessionResponse(
        String sessionId,
        String sessionToken,
        String storeName,
        LocalDateTime expiresAt
) {
}
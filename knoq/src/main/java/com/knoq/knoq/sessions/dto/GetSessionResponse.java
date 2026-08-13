package com.knoq.knoq.sessions.dto;

import java.time.LocalDateTime;

public record GetSessionResponse(
        String sessionId,
        String storeName,
        LocalDateTime expiresAt
) {}
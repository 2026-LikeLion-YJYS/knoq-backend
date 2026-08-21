package com.knoq.knoq.sessions.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ConsentsResponse(
        List<ConsentItem> consents
) {
    public record ConsentItem(
            ConsentType type,
            boolean agreed,
            LocalDateTime agreedAt
    ) {}
}
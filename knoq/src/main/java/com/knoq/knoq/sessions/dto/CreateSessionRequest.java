package com.knoq.knoq.sessions.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSessionRequest(
        @NotBlank(message = "storeCode는 필수입니다.")
        String storeCode
) {
}
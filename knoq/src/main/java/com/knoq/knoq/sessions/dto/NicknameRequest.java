package com.knoq.knoq.sessions.dto;

import jakarta.validation.constraints.Size;

public record NicknameRequest(
        @Size(min = 2, max = 10, message = "닉네임은 2~10자여야 합니다.")
        String nickname // 생략(null) 가능 — 없으면 서버가 자동 생성
) {}
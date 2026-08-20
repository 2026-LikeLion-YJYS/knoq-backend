package com.knoq.knoq.sessions.dto;

import com.knoq.knoq.sessions.entity.LifestyleTag;
import com.knoq.knoq.sessions.entity.StorageScope;

import java.time.LocalDateTime;
import java.util.List;

// 새로고침/재진입 시 프론트가 로그인·저장범위·닉네임·라이프스타일 상태를 복원할 수 있도록
// storageScope, nickname, lifestyleTags 추가 (기존엔 sessionId/storeName/expiresAt만 있었음)
public record GetSessionResponse(
        String sessionId,
        String storeName,
        StorageScope storageScope,
        String nickname,
        List<LifestyleTag> lifestyleTags,
        LocalDateTime expiresAt
) {
}

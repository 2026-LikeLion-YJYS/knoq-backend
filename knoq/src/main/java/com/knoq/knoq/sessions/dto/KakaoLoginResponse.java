package com.knoq.knoq.sessions.dto;

import com.knoq.knoq.sessions.entity.StorageScope;

public record KakaoLoginResponse(
        StorageScope storageScope,
        String accountId // 로그인 실패 시 null
) {}
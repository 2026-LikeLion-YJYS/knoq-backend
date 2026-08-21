package com.knoq.knoq.sessions.dto;

import com.knoq.knoq.sessions.entity.LifestyleTag;
import com.knoq.knoq.sessions.entity.StorageScope;

import java.util.List;

public record KakaoLoginResponse(
        StorageScope storageScope,
        String accountId, // 로그인 실패 시 null
        String nickname,
        List<LifestyleTag> lifestyleTags,
        boolean onboardingCompleted
) {
    public KakaoLoginResponse {
        lifestyleTags = lifestyleTags == null ? List.of() : List.copyOf(lifestyleTags);
    }
}

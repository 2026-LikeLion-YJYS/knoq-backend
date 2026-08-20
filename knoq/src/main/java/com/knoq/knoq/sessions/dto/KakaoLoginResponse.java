package com.knoq.knoq.sessions.dto;

import com.knoq.knoq.sessions.entity.LifestyleTag;
import com.knoq.knoq.sessions.entity.StorageScope;

import java.util.List;

public record KakaoLoginResponse(
        StorageScope storageScope,
        String accountId, // 로그인 실패 시 null

        // 오늘 같은 매장에서 이미 로그인한 적이 있으면, 그 기존 세션으로 갈아탄다.
        // 클라이언트는 이 시점부터 원래 쓰던 sessionId 대신 이 값들로 이후 요청을 보내야 함
        // (기존 sessionId와 같으면 그대로 써도 됨)
        String sessionId,
        String sessionToken,

        // onboardingCompleted=false일 때는 "이전에 쓰던 닉네임" 추천값(수정 가능, 자동 확정 아님)
        // onboardingCompleted=true일 때는 실제로 확정된 닉네임
        String nickname,
        List<LifestyleTag> lifestyleTags,
        boolean onboardingCompleted
) {
    public KakaoLoginResponse {
        lifestyleTags = lifestyleTags == null ? List.of() : List.copyOf(lifestyleTags);
    }
}

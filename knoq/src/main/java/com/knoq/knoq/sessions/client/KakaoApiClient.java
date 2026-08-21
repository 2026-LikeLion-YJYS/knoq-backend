package com.knoq.knoq.sessions.client;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * 카카오 사용자 정보 API(v2/user/me) 호출 전용 클라이언트.
 * 클라이언트 앱이 이미 카카오 SDK로 로그인해서 받아온 kakaoAccessToken을 그대로 넘겨받아,
 * 그 토큰이 진짜 유효한지 + 고유 회원번호(id)가 뭔지를 카카오 서버에 직접 물어봄.
 * 이메일/닉네임 등 추가 정보는 요청하지 않음(스코프 자체를 안 씀).
 */
@Component
public class KakaoApiClient {

    private final RestClient restClient = RestClient.create("https://kapi.kakao.com");

    public Optional<Long> getKakaoUserId(String kakaoAccessToken) {
        try {
            KakaoUserResponse response = restClient.get()
                    .uri("/v2/user/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
                    .retrieve()
                    .body(KakaoUserResponse.class);

            return Optional.ofNullable(response).map(KakaoUserResponse::id);
        } catch (Exception e) {
            // 토큰이 만료됐거나, 카카오 서버가 응답 안 하거나, 뭐가 됐든
            // 여기서 예외를 던지지 않고 "실패했다"는 것만 알려줌 (호출한 쪽에서 실패 처리하게)
            return Optional.empty();
        }
    }

    // 카카오 응답 JSON에 이것 말고도 필드가 많지만, 우리는 id만 필요해서 그것만 받음
    private record KakaoUserResponse(Long id) {}
}
package com.knoq.knoq.sessions.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.sessions.client.KakaoApiClient;
import com.knoq.knoq.sessions.dto.ConsentRequest;
import com.knoq.knoq.sessions.dto.ConsentsResponse;
import com.knoq.knoq.sessions.dto.CreateSessionRequest;
import com.knoq.knoq.sessions.dto.CreateSessionResponse;
import com.knoq.knoq.sessions.dto.GetSessionResponse;
import com.knoq.knoq.sessions.dto.KakaoLoginRequest;
import com.knoq.knoq.sessions.dto.KakaoLoginResponse;
import com.knoq.knoq.sessions.dto.LifestyleTagsRequest;
import com.knoq.knoq.sessions.dto.LifestyleTagsResponse;
import com.knoq.knoq.sessions.dto.NicknameRequest;
import com.knoq.knoq.sessions.dto.NicknameResponse;
import com.knoq.knoq.sessions.dto.StorageScopeRequest;
import com.knoq.knoq.sessions.dto.StorageScopeResponse;
import com.knoq.knoq.sessions.entity.LifestyleTag;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.entity.StorageScope;
import com.knoq.knoq.sessions.event.SessionFinishedEvent;
import com.knoq.knoq.sessions.repository.SessionRepository;
import com.knoq.knoq.store.entity.Store;
import com.knoq.knoq.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@RecordApplicationEvents // 이 테스트 안에서 발행된 이벤트를 기록해뒀다가 나중에 확인할 수 있게 해줌
@Transactional // 테스트 끝나면 저장한 데이터는 자동으로 롤백 (실제 DB에 안 남음)
class SessionServiceTest {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SessionRepository sessionRepository;

    // 진짜 카카오 서버에 요청 안 보내고, 이 테스트 안에서만 가짜로 동작을 지정할 수 있는 가짜 객체로 대체
    @MockitoBean
    private KakaoApiClient kakaoApiClient;

    private Store testStore;

    @BeforeEach
    void setUp() {
        String storeCode = "SESSION-TEST-" + UUID.randomUUID().toString().substring(0, 8);
        testStore = storeRepository.save(Store.of(storeCode, "테스트 매장"));
    }

    @Test
    void 유효한_매장코드로_요청하면_세션을_생성한다() {
        CreateSessionRequest request = new CreateSessionRequest(testStore.getStoreCode());

        CreateSessionResponse response = sessionService.createSession(request);

        assertThat(response.sessionId()).startsWith("sess_");
        assertThat(response.sessionToken()).isNotBlank();
        assertThat(response.storeName()).isEqualTo("테스트 매장");
        assertThat(response.expiresAt()).isNotNull();
    }

    @Test
    void 존재하지_않는_매장코드면_예외를_던진다() {
        CreateSessionRequest request = new CreateSessionRequest("NOT-EXIST");

        assertThatThrownBy(() -> sessionService.createSession(request))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void 존재하는_세션을_조회하면_정보를_반환한다() {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));

        GetSessionResponse response = sessionService.getSession(created.sessionId());

        assertThat(response.sessionId()).isEqualTo(created.sessionId());
        assertThat(response.storeName()).isEqualTo("테스트 매장");
        assertThat(response.expiresAt()).isEqualTo(created.expiresAt());
        // 새로고침 시 프론트가 상태 복원할 수 있게 storageScope/nickname/lifestyleTags도 응답에 포함
        assertThat(response.storageScope()).isEqualTo(StorageScope.PRIVATE);
        assertThat(response.nickname()).isNull();
        assertThat(response.lifestyleTags()).isEmpty();
    }

    @Test
    void 세션_조회는_폴링_API이므로_만료시간을_갱신하지_않는다() {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));

        sessionService.getSession(created.sessionId());

        Session found = sessionRepository.findById(created.sessionId()).orElseThrow();
        assertThat(found.getExpiresAt()).isEqualTo(created.expiresAt());
    }

    @Test
    void 실제_사용자_동작_API는_세션_만료시간을_갱신한다() {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));

        sessionService.selectStorageScope(
                created.sessionId(), new StorageScopeRequest(StorageScope.PRIVATE)
        );

        Session found = sessionRepository.findById(created.sessionId()).orElseThrow();
        assertThat(found.getExpiresAt()).isAfter(created.expiresAt());
    }

    @Test
    void 존재하지_않는_세션ID면_예외를_던진다() {
        assertThatThrownBy(() -> sessionService.getSession("sess_not_exist"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void 만료된_세션이면_예외를_던진다() {
        // 이미 만료 시각이 지난 세션을 직접 만들어서 저장
        Session expiredSession = Session.of(
                "sess_expired_test",
                "token_expired_test",
                testStore.getId(),
                LocalDateTime.now().minusMinutes(1) // 1분 전 = 이미 만료됨
        );
        sessionRepository.save(expiredSession);

        assertThatThrownBy(() -> sessionService.getSession("sess_expired_test"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void 필수_약관에_모두_동의하면_저장된다() {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));
        ConsentRequest request = new ConsentRequest(true, true, true, false);

        ConsentsResponse response = sessionService.agreeConsents(created.sessionId(), request);

        assertThat(response.consents()).hasSize(4);
        assertThat(response.consents())
                .allSatisfy(item -> assertThat(item.agreedAt()).isNotNull());
    }

    @Test
    void 필수_약관을_하나라도_동의안하면_예외를_던진다() {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));
        ConsentRequest request = new ConsentRequest(true, true, false, false); // over14 미동의

        assertThatThrownBy(() -> sessionService.agreeConsents(created.sessionId(), request))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void PRIVATE를_선택하면_PRIVATE로_유지된다() {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));

        StorageScopeResponse response = sessionService.selectStorageScope(
                created.sessionId(), new StorageScopeRequest(StorageScope.PRIVATE)
        );

        assertThat(response.storageScope()).isEqualTo(StorageScope.PRIVATE);
        assertThat(response.kakaoLoginRequired()).isFalse();
    }

    @Test
    void ACCOUNT를_선택하면_카카오로그인_대기_상태가_된다() {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));

        StorageScopeResponse response = sessionService.selectStorageScope(
                created.sessionId(), new StorageScopeRequest(StorageScope.ACCOUNT)
        );

        assertThat(response.storageScope()).isEqualTo(StorageScope.PENDING_KAKAO_LOGIN);
        assertThat(response.kakaoLoginRequired()).isTrue();
    }

    @Test
    void 카카오_로그인에_성공하면_계정이_연결된다() {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));
        when(kakaoApiClient.getKakaoUserId("valid-token")).thenReturn(Optional.of(123456789L));

        KakaoLoginResponse response = sessionService.kakaoLogin(
                created.sessionId(), new KakaoLoginRequest("valid-token")
        );

        assertThat(response.storageScope()).isEqualTo(StorageScope.ACCOUNT);
        assertThat(response.accountId()).startsWith("acct_");
        assertThat(response.nickname()).isNull();
        assertThat(response.lifestyleTags()).isEmpty();
        assertThat(response.onboardingCompleted()).isFalse();
    }

    @Test
    void 재방문_카카오_계정은_최근_온보딩_정보를_복원한다() {
        when(kakaoApiClient.getKakaoUserId("returning-token")).thenReturn(Optional.of(987654321L));

        CreateSessionResponse previous = sessionService.createSession(
                new CreateSessionRequest(testStore.getStoreCode())
        );
        sessionService.kakaoLogin(previous.sessionId(), new KakaoLoginRequest("returning-token"));
        sessionService.setNickname(previous.sessionId(), new NicknameRequest("레몬토끼"));
        sessionService.updateLifestyleTags(
                previous.sessionId(),
                new LifestyleTagsRequest(List.of(LifestyleTag.MINIMAL, LifestyleTag.CLASSIC))
        );
        sessionService.finishShopping(previous.sessionId());

        CreateSessionResponse current = sessionService.createSession(
                new CreateSessionRequest(testStore.getStoreCode())
        );
        KakaoLoginResponse response = sessionService.kakaoLogin(
                current.sessionId(), new KakaoLoginRequest("returning-token")
        );

        assertThat(response.storageScope()).isEqualTo(StorageScope.ACCOUNT);
        assertThat(response.accountId()).startsWith("acct_");
        assertThat(response.nickname()).isEqualTo("레몬토끼");
        assertThat(response.lifestyleTags())
                .containsExactly(LifestyleTag.MINIMAL, LifestyleTag.CLASSIC);
        assertThat(response.onboardingCompleted()).isTrue();

        GetSessionResponse restored = sessionService.getSession(current.sessionId());
        assertThat(restored.nickname()).isEqualTo("레몬토끼");
        assertThat(restored.lifestyleTags())
                .containsExactly(LifestyleTag.MINIMAL, LifestyleTag.CLASSIC);
    }

    @Test
    void 카카오_로그인에_실패해도_에러_없이_PRIVATE로_응답한다() {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));
        when(kakaoApiClient.getKakaoUserId("invalid-token")).thenReturn(Optional.empty());

        KakaoLoginResponse response = sessionService.kakaoLogin(
                created.sessionId(), new KakaoLoginRequest("invalid-token")
        );

        assertThat(response.storageScope()).isEqualTo(StorageScope.PRIVATE);
        assertThat(response.accountId()).isNull();
        assertThat(response.nickname()).isNull();
        assertThat(response.lifestyleTags()).isEmpty();
        assertThat(response.onboardingCompleted()).isFalse();
    }

    @Test
    void 닉네임을_지정하면_그대로_저장된다() {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));

        NicknameResponse response = sessionService.setNickname(created.sessionId(), new NicknameRequest("민트라떼"));

        assertThat(response.nickname()).isEqualTo("민트라떼");
        assertThat(response.autoGenerated()).isFalse();
    }

    @Test
    void 닉네임을_생략하면_자동_생성된다() {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));

        NicknameResponse response = sessionService.setNickname(created.sessionId(), new NicknameRequest(null));

        assertThat(response.nickname()).isNotBlank();
        assertThat(response.autoGenerated()).isTrue();
    }

    @Test
    void 금칙어가_포함되면_예외를_던진다() {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));

        assertThatThrownBy(() -> sessionService.setNickname(created.sessionId(), new NicknameRequest("병신라떼")))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void 라이프스타일_태그를_1개에서_3개_고르면_저장된다() {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));
        LifestyleTagsRequest request = new LifestyleTagsRequest(List.of(LifestyleTag.MINIMAL, LifestyleTag.CASUAL));

        LifestyleTagsResponse response = sessionService.updateLifestyleTags(created.sessionId(), request);

        assertThat(response.tags()).containsExactly(LifestyleTag.MINIMAL, LifestyleTag.CASUAL);
    }

    @Test
    void 라이프스타일_태그를_하나도_안_고르면_예외를_던진다() {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));
        LifestyleTagsRequest request = new LifestyleTagsRequest(List.of());

        assertThatThrownBy(() -> sessionService.updateLifestyleTags(created.sessionId(), request))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void 라이프스타일_태그를_4개_이상_고르면_예외를_던진다() {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));
        LifestyleTagsRequest request = new LifestyleTagsRequest(List.of(
                LifestyleTag.MINIMAL, LifestyleTag.CASUAL, LifestyleTag.STREET, LifestyleTag.FORMAL
        ));

        assertThatThrownBy(() -> sessionService.updateLifestyleTags(created.sessionId(), request))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void PRIVATE_세션은_쇼핑_마치면_하드_삭제된다(ApplicationEvents events) {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));

        sessionService.finishShopping(created.sessionId());

        assertThat(sessionRepository.findById(created.sessionId())).isEmpty();
        assertThat(events.stream(SessionFinishedEvent.class)).hasSize(1);
    }

    @Test
    void ACCOUNT_세션은_쇼핑_마치면_삭제되지_않고_즉시_만료된다(ApplicationEvents events) {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));
        when(kakaoApiClient.getKakaoUserId("valid-token")).thenReturn(Optional.of(999L));
        sessionService.kakaoLogin(created.sessionId(), new KakaoLoginRequest("valid-token")); // ACCOUNT로 전환

        sessionService.finishShopping(created.sessionId());

        Session found = sessionRepository.findById(created.sessionId()).orElseThrow();
        assertThat(found.getExpiresAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(events.stream(SessionFinishedEvent.class)).hasSize(1);
    }

    @Test
    void ACCOUNT_세션에_로그아웃하면_PRIVATE로_돌아간다() {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));
        when(kakaoApiClient.getKakaoUserId("valid-token")).thenReturn(Optional.of(888L));
        sessionService.kakaoLogin(created.sessionId(), new KakaoLoginRequest("valid-token")); // ACCOUNT로 전환

        sessionService.logout(created.sessionId());

        Session found = sessionRepository.findById(created.sessionId()).orElseThrow();
        assertThat(found.getStorageScope()).isEqualTo(StorageScope.PRIVATE);
        assertThat(found.getAccountId()).isNull();
    }

    @Test
    void 이미_PRIVATE인_세션에_로그아웃해도_에러_없이_그대로다() {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest(testStore.getStoreCode()));

        sessionService.logout(created.sessionId());

        Session found = sessionRepository.findById(created.sessionId()).orElseThrow();
        assertThat(found.getStorageScope()).isEqualTo(StorageScope.PRIVATE);
    }
}

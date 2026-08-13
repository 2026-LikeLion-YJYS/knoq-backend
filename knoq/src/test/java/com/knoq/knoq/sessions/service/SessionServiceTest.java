package com.knoq.knoq.sessions.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.sessions.dto.ConsentRequest;
import com.knoq.knoq.sessions.dto.ConsentsResponse;
import com.knoq.knoq.sessions.dto.CreateSessionRequest;
import com.knoq.knoq.sessions.dto.CreateSessionResponse;
import com.knoq.knoq.sessions.dto.GetSessionResponse;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.repository.SessionRepository;
import com.knoq.knoq.store.entity.Store;
import com.knoq.knoq.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional // 테스트 끝나면 저장한 데이터는 자동으로 롤백 (실제 DB에 안 남음)
class SessionServiceTest {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SessionRepository sessionRepository;

    private Store testStore;

    @BeforeEach
    void setUp() {
        testStore = storeRepository.save(Store.of("TEST-001", "테스트 매장"));
    }

    @Test
    void 유효한_매장코드로_요청하면_세션을_생성한다() {
        CreateSessionRequest request = new CreateSessionRequest("TEST-001");

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
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest("TEST-001"));

        GetSessionResponse response = sessionService.getSession(created.sessionId());

        assertThat(response.sessionId()).isEqualTo(created.sessionId());
        assertThat(response.storeName()).isEqualTo("테스트 매장");
        assertThat(response.expiresAt()).isEqualTo(created.expiresAt());
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
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest("TEST-001"));
        ConsentRequest request = new ConsentRequest(true, true, true, false);

        ConsentsResponse response = sessionService.agreeConsents(created.sessionId(), request);

        assertThat(response.consents()).hasSize(4);
        assertThat(response.consents())
                .allSatisfy(item -> assertThat(item.agreedAt()).isNotNull());
    }

    @Test
    void 필수_약관을_하나라도_동의안하면_예외를_던진다() {
        CreateSessionResponse created = sessionService.createSession(new CreateSessionRequest("TEST-001"));
        ConsentRequest request = new ConsentRequest(true, true, false, false); // over14 미동의

        assertThatThrownBy(() -> sessionService.agreeConsents(created.sessionId(), request))
                .isInstanceOf(ApiException.class);
    }
}
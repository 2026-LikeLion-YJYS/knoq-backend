package com.knoq.knoq.sessions.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.repository.SessionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "knoq.session.expiry-minutes=60")
@Transactional
class SessionExpirationServiceTest {

    @Autowired
    private SessionExpirationService sessionExpirationService;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private EntityManager entityManager;

    private Session session;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        session = Session.of(
                "sess_expiration_" + suffix,
                "token_expiration_" + suffix,
                1L,
                LocalDateTime.now().plusMinutes(5)
        );
        sessionRepository.save(session);
    }

    @Test
    @DisplayName("실제 사용자 동작은 세션 만료시간을 설정된 유지시간만큼 갱신한다")
    void refresh_extends_session_expiration() {
        LocalDateTime expiresAtBeforeAction = session.getExpiresAt();

        sessionExpirationService.getValidSessionAndRefresh(session.getId());
        entityManager.flush();
        entityManager.clear();

        LocalDateTime expiresAtAfterAction = sessionRepository.findById(session.getId())
                .orElseThrow()
                .getExpiresAt();
        assertThat(expiresAtAfterAction).isAfter(expiresAtBeforeAction);
    }

    @Test
    @DisplayName("폴링 조회는 세션 만료시간을 갱신하지 않는다")
    void validation_only_does_not_extend_session_expiration() {
        LocalDateTime expiresAtBeforePolling = session.getExpiresAt();

        sessionExpirationService.getValidSession(session.getId());
        entityManager.flush();
        entityManager.clear();

        LocalDateTime expiresAtAfterPolling = sessionRepository.findById(session.getId())
                .orElseThrow()
                .getExpiresAt();
        assertThat(expiresAtAfterPolling).isEqualTo(expiresAtBeforePolling);
    }

    @Test
    @DisplayName("만료된 세션은 갱신하지 않고 410 예외를 반환한다")
    void expired_session_is_not_refreshed() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        Session expiredSession = Session.of(
                "sess_expired_" + suffix,
                "token_expired_" + suffix,
                1L,
                LocalDateTime.now().minusMinutes(1)
        );
        sessionRepository.save(expiredSession);

        assertThatThrownBy(() -> sessionExpirationService.getValidSessionAndRefresh(expiredSession.getId()))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_EXPIRED);
    }
}

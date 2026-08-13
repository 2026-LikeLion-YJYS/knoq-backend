package com.knoq.knoq.sessions.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.sessions.dto.CreateSessionRequest;
import com.knoq.knoq.sessions.dto.CreateSessionResponse;
import com.knoq.knoq.store.entity.Store;
import com.knoq.knoq.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional // 테스트 끝나면 저장한 데이터는 자동으로 롤백 (실제 DB에 안 남음)
class SessionServiceTest {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private StoreRepository storeRepository;

    @BeforeEach
    void setUp() {
        storeRepository.save(Store.of("TEST-001", "테스트 매장"));
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
}
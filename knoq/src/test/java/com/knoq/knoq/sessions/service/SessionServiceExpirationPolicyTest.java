package com.knoq.knoq.sessions.service;

import com.knoq.knoq.account.repository.AccountRepository;
import com.knoq.knoq.sessions.client.KakaoApiClient;
import com.knoq.knoq.sessions.dto.GetSessionResponse;
import com.knoq.knoq.sessions.dto.StorageScopeRequest;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.entity.StorageScope;
import com.knoq.knoq.sessions.repository.SessionRepository;
import com.knoq.knoq.store.entity.Store;
import com.knoq.knoq.store.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceExpirationPolicyTest {

    @InjectMocks
    private SessionService sessionService;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private KakaoApiClient kakaoApiClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SessionExpirationService sessionExpirationService;

    @Test
    void 세션_조회는_만료시간을_갱신하지_않는다() {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        Session session = Session.of("sess_test", "token_test", 1L, expiresAt);
        Store store = Store.of("TEST-001", "MCM 청담 HAUS");
        when(sessionExpirationService.getValidSession("sess_test")).thenReturn(session);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        GetSessionResponse response = sessionService.getSession("sess_test");

        assertThat(response.expiresAt()).isEqualTo(expiresAt);
        verify(sessionExpirationService).getValidSession("sess_test");
        verify(sessionExpirationService, never()).getValidSessionAndRefresh("sess_test");
    }

    @Test
    void 저장범위_선택은_실제_사용자_동작이므로_만료시간을_갱신한다() {
        Session session = Session.of(
                "sess_test", "token_test", 1L, LocalDateTime.now().plusMinutes(30)
        );
        when(sessionExpirationService.getValidSessionAndRefresh("sess_test")).thenReturn(session);

        sessionService.selectStorageScope(
                "sess_test", new StorageScopeRequest(StorageScope.PRIVATE)
        );

        verify(sessionExpirationService).getValidSessionAndRefresh("sess_test");
        verify(sessionExpirationService, never()).getValidSession("sess_test");
    }
}

package com.knoq.knoq.sessions.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.product.entity.Product;
import com.knoq.knoq.product.repository.ProductRepository;
import com.knoq.knoq.saved.service.SavedProductService;
import com.knoq.knoq.sessions.client.KakaoApiClient;
import com.knoq.knoq.sessions.dto.CreateSessionRequest;
import com.knoq.knoq.sessions.dto.CreateSessionResponse;
import com.knoq.knoq.sessions.dto.KakaoLoginRequest;
import com.knoq.knoq.sessions.dto.SessionArchiveResponse;
import com.knoq.knoq.store.entity.Store;
import com.knoq.knoq.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class SessionArchiveServiceTest {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SavedProductService savedProductService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private KakaoApiClient kakaoApiClient;

    private Store store;
    private String productId;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        store = storeRepository.save(Store.of("ARCHIVE-" + suffix, "아카이브 테스트 매장"));
        productId = "prod_archive_" + suffix;
        productRepository.save(Product.of(
                productId,
                "ARCHIVE-PRODUCT-" + suffix,
                "테스트 가방",
                "가죽",
                "아카이브 테스트용",
                100_000L,
                List.of("M"),
                List.of("블랙"),
                "/products/archive.png",
                "설명",
                "AI 설명"
        ));
    }

    @Test
    void 같은_계정의_방문과_저장제품을_최근순으로_반환한다() {
        when(kakaoApiClient.getKakaoUserId("same-account"))
                .thenReturn(Optional.of(12345L));

        CreateSessionResponse previous = createAccountSession();
        savedProductService.saveFromCamera(previous.sessionId(), productId);
        sessionService.finishShopping(previous.sessionId());

        CreateSessionResponse current = createAccountSession();

        SessionArchiveResponse response = sessionService.getArchive(current.sessionId());

        assertThat(response.count()).isEqualTo(2);
        assertThat(response.visits()).extracting(SessionArchiveResponse.Visit::sessionId)
                .containsExactly(current.sessionId(), previous.sessionId());
        assertThat(response.visits().get(0).isCurrent()).isTrue();
        assertThat(response.visits().get(1).isCurrent()).isFalse();
        assertThat(response.visits().get(1).products()).hasSize(1);
        assertThat(response.visits().get(1).products().get(0).name()).isEqualTo("테스트 가방");
        assertThat(response.visits().get(1).products().get(0).thumbnailUrl())
                .isEqualTo("/products/archive.png");
    }

    @Test
    void 계정에_연결되지_않은_세션은_아카이브를_조회할_수_없다() {
        CreateSessionResponse privateSession = sessionService.createSession(
                new CreateSessionRequest(store.getStoreCode())
        );

        assertThatThrownBy(() -> sessionService.getArchive(privateSession.sessionId()))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_LOGIN_REQUIRED)
                );
    }

    private CreateSessionResponse createAccountSession() {
        CreateSessionResponse session = sessionService.createSession(
                new CreateSessionRequest(store.getStoreCode())
        );
        sessionService.kakaoLogin(session.sessionId(), new KakaoLoginRequest("same-account"));
        return session;
    }
}

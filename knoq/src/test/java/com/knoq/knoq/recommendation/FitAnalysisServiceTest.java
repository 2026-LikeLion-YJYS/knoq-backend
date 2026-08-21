package com.knoq.knoq.recommendation;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.product.entity.Product;
import com.knoq.knoq.product.repository.ProductRepository;
import com.knoq.knoq.recommendation.dto.response.FitAnalysisResponse;
import com.knoq.knoq.recommendation.service.FitAnalysisGenerator;
import com.knoq.knoq.recommendation.service.FitAnalysisService;
import com.knoq.knoq.sessions.entity.LifestyleTag;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.RecordComponent;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class FitAnalysisServiceTest {

    @Autowired
    private FitAnalysisService fitAnalysisService;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private FitAnalysisGenerator fitAnalysisGenerator;

    private Session session;
    private Product product;
    private String testSuffix;

    @BeforeEach
    void setUp() {
        testSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        session = Session.of(
                "sess_fit_" + testSuffix,
                "token_fit_" + testSuffix,
                1L,
                LocalDateTime.now().plusMinutes(30)
        );
        session.updateLifestyleTags(List.of(LifestyleTag.MINIMAL));
        sessionRepository.save(session);

        product = Product.of(
                "prod_fit_" + testSuffix,
                "FIT-" + testSuffix,
                "미니멀 가죽 가방",
                "가죽",
                "심플한 블랙 디자인",
                100_000L,
                List.of("M"),
                List.of("블랙"),
                "https://example.com/fit.jpg",
                "심플한 데일리 가방",
                null
        );
        product.updateCategory("가방");
        productRepository.save(product);
    }

    @Test
    @DisplayName("LLM 분석 결과가 있으면 summary, reasons, cautions를 그대로 반환한다")
    void analyze_returns_generated_result() {
        FitAnalysisResponse generated = new FitAnalysisResponse(
                "미니멀 라이프스타일과 잘 어울리는 제품이에요.",
                List.of("심플한 블랙 디자인이 미니멀한 취향과 잘 맞아요."),
                List.of("실제 가죽 질감은 매장에서 확인해 주세요.")
        );
        when(fitAnalysisGenerator.generate(anyList(), any(Product.class))).thenReturn(generated);

        FitAnalysisResponse response = fitAnalysisService.analyze(session.getId(), product.getId());

        assertThat(response).isEqualTo(generated);
    }

    @Test
    @DisplayName("LLM 호출이 실패하면 제품 속성을 이용한 규칙 기반 결과를 반환한다")
    void analyze_falls_back_when_generation_fails() {
        when(fitAnalysisGenerator.generate(anyList(), any(Product.class))).thenReturn(null);

        FitAnalysisResponse response = fitAnalysisService.analyze(session.getId(), product.getId());

        assertThat(response.summary()).contains("MINIMAL");
        assertThat(response.reasons()).singleElement().asString().contains("미니멀");
        assertThat(response.cautions()).isNotEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 세션이면 404 예외가 발생한다")
    void analyze_fails_when_session_does_not_exist() {
        assertThatThrownBy(() -> fitAnalysisService.analyze("sess_not_exist", product.getId()))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_FOUND);
    }

    @Test
    @DisplayName("만료된 세션이면 410 예외가 발생한다")
    void analyze_fails_when_session_is_expired() {
        Session expiredSession = Session.of(
                "sess_fit_expired_" + testSuffix,
                "token_fit_expired_" + testSuffix,
                1L,
                LocalDateTime.now().minusMinutes(1)
        );
        expiredSession.updateLifestyleTags(List.of(LifestyleTag.MINIMAL));
        sessionRepository.save(expiredSession);

        assertThatThrownBy(() -> fitAnalysisService.analyze(expiredSession.getId(), product.getId()))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_EXPIRED);
    }

    @Test
    @DisplayName("존재하지 않는 제품이면 404 예외가 발생한다")
    void analyze_fails_when_product_does_not_exist() {
        assertThatThrownBy(() -> fitAnalysisService.analyze(session.getId(), "prod_not_exist"))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("적합 분석 응답에는 명세의 세 필드만 존재한다")
    void response_contains_only_specified_fields() {
        assertThat(Arrays.stream(FitAnalysisResponse.class.getRecordComponents())
                .map(RecordComponent::getName)
                .toList())
                .containsExactly("summary", "reasons", "cautions");
    }
}

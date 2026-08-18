package com.knoq.knoq.consultation;

import com.knoq.knoq.consultation.dto.request.CreateConsultationRequest;
import com.knoq.knoq.consultation.dto.response.CreateConsultationResponse;
import com.knoq.knoq.consultation.dto.response.ConsultationStatusResponse;
import com.knoq.knoq.consultation.entity.ConsultationRequest;
import com.knoq.knoq.consultation.entity.HelpType;
import com.knoq.knoq.consultation.entity.RequestStatus;
import com.knoq.knoq.consultation.repository.ConsultationRequestRepository;
import com.knoq.knoq.consultation.service.ConsultationRequestService;
import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.product.entity.Product;
import com.knoq.knoq.product.repository.ProductRepository;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ConsultationRequestServiceTest {

    @Autowired
    private ConsultationRequestService consultationRequestService;

    @Autowired
    private ConsultationRequestRepository consultationRequestRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ProductRepository productRepository;

    private Session session;

    @BeforeEach
    void setUp() {
        session = Session.of(
                "sess_consultation_test",
                "token_consultation_test",
                1L,
                LocalDateTime.now().plusMinutes(30)
        );
        sessionRepository.save(session);
    }

    @Test
    @DisplayName("상담 요청을 생성하면 REQUESTED 상태와 req_ ID를 반환한다")
    void create_success() {
        Product productA = saveProduct("prod_consult_1", "PD-CONSULT-1");
        Product productB = saveProduct("prod_consult_2", "PD-CONSULT-2");

        CreateConsultationResponse response = consultationRequestService.create(
                session.getId(),
                new CreateConsultationRequest(
                        HelpType.PRODUCT_COMPARISON,
                        List.of(productA.getId(), productB.getId()),
                        true
                )
        );

        assertThat(response.requestId()).startsWith("req_");
        assertThat(response.status()).isEqualTo(RequestStatus.REQUESTED);
        assertThat(response.requestedAt()).isNotNull();

        ConsultationRequest saved = consultationRequestRepository.findById(response.requestId()).orElseThrow();
        assertThat(saved.getSessionId()).isEqualTo(session.getId());
        assertThat(saved.getStoreId()).isEqualTo(session.getStoreId());
        assertThat(saved.getHelpType()).isEqualTo(HelpType.PRODUCT_COMPARISON);
        assertThat(saved.isIncludeNeedsAnalysis()).isTrue();
        assertThat(saved.getProducts())
                .extracting(product -> product.getProductId())
                .containsExactly(productA.getId(), productB.getId());
    }

    @Test
    @DisplayName("제품을 3개 초과하면 검증 예외가 발생한다")
    void create_fails_when_products_exceed_three() {
        assertValidationError(new CreateConsultationRequest(
                HelpType.PRODUCT_INFO,
                List.of("prod_1", "prod_2", "prod_3", "prod_4"),
                false
        ));
    }

    @Test
    @DisplayName("제품 비교는 제품이 2개 미만이면 검증 예외가 발생한다")
    void create_fails_when_comparison_has_less_than_two_products() {
        Product product = saveProduct("prod_compare_1", "PD-COMPARE-1");

        assertValidationError(new CreateConsultationRequest(
                HelpType.PRODUCT_COMPARISON,
                List.of(product.getId()),
                false
        ));
    }

    @Test
    @DisplayName("세션에 활성 상담 요청이 있으면 409 예외가 발생한다")
    void create_fails_when_active_request_exists() {
        Product product = saveProduct("prod_duplicate_1", "PD-DUPLICATE-1");
        CreateConsultationRequest request = new CreateConsultationRequest(
                HelpType.PRODUCT_INFO,
                List.of(product.getId()),
                false
        );
        consultationRequestService.create(session.getId(), request);

        assertThatThrownBy(() -> consultationRequestService.create(session.getId(), request))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVE_CONSULTATION_REQUEST_EXISTS);
    }

    @Test
    @DisplayName("존재하지 않는 제품이 포함되면 404 예외가 발생한다")
    void create_fails_when_product_does_not_exist() {
        assertThatThrownBy(() -> consultationRequestService.create(
                session.getId(),
                new CreateConsultationRequest(
                        HelpType.PRODUCT_INFO,
                        List.of("prod_not_exist"),
                        false
                )
        ))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("만료된 세션으로 요청하면 410 예외가 발생한다")
    void create_fails_when_session_is_expired() {
        Session expiredSession = Session.of(
                "sess_consultation_expired",
                "token_consultation_expired",
                1L,
                LocalDateTime.now().minusMinutes(1)
        );
        sessionRepository.save(expiredSession);

        assertThatThrownBy(() -> consultationRequestService.create(
                expiredSession.getId(),
                new CreateConsultationRequest(HelpType.PRODUCT_RECOMMENDATION, List.of(), false)
        ))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_EXPIRED);
    }

    @Test
    @DisplayName("상담 요청에 금지된 개인정보 필드를 저장하지 않는다")
    void consultation_request_does_not_contain_prohibited_personal_information() {
        Set<String> fieldNames = Arrays.stream(ConsultationRequest.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertThat(fieldNames).doesNotContain(
                "realName", "face", "contact", "budget", "purchaseHistory", "preciseLocation"
        );
    }

    @Test
    @DisplayName("상담 요청 상태를 조회하면 현재 상태와 갱신 시각을 반환한다")
    void get_status_success() {
        CreateConsultationResponse created = createConsultationRequest(session.getId());

        ConsultationStatusResponse response = consultationRequestService.getStatus(
                session.getId(), created.requestId()
        );

        assertThat(response.requestId()).isEqualTo(created.requestId());
        assertThat(response.status()).isEqualTo(RequestStatus.REQUESTED);
        assertThat(response.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("상태 조회는 세션 만료시각을 갱신하지 않는다")
    void get_status_does_not_extend_session_expiration() {
        CreateConsultationResponse created = createConsultationRequest(session.getId());
        LocalDateTime expiresAtBeforePolling = session.getExpiresAt();

        consultationRequestService.getStatus(session.getId(), created.requestId());

        LocalDateTime expiresAtAfterPolling = sessionRepository.findById(session.getId())
                .orElseThrow()
                .getExpiresAt();
        assertThat(expiresAtAfterPolling).isEqualTo(expiresAtBeforePolling);
    }

    @Test
    @DisplayName("존재하지 않는 상담 요청을 조회하면 404 예외가 발생한다")
    void get_status_fails_when_request_does_not_exist() {
        assertThatThrownBy(() -> consultationRequestService.getStatus(session.getId(), "req_not_exist"))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONSULTATION_REQUEST_NOT_FOUND);
    }

    @Test
    @DisplayName("다른 세션의 상담 요청을 조회하면 404 예외가 발생한다")
    void get_status_fails_when_request_belongs_to_another_session() {
        CreateConsultationResponse created = createConsultationRequest(session.getId());
        Session anotherSession = Session.of(
                "sess_consultation_another",
                "token_consultation_another",
                1L,
                LocalDateTime.now().plusMinutes(30)
        );
        sessionRepository.save(anotherSession);

        assertThatThrownBy(() -> consultationRequestService.getStatus(
                anotherSession.getId(), created.requestId()
        ))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONSULTATION_REQUEST_NOT_FOUND);
    }

    @Test
    @DisplayName("만료된 세션으로 상태를 조회하면 410 예외가 발생한다")
    void get_status_fails_when_session_is_expired() {
        CreateConsultationResponse created = createConsultationRequest(session.getId());
        Session expiredSession = Session.of(
                "sess_status_expired",
                "token_status_expired",
                1L,
                LocalDateTime.now().minusMinutes(1)
        );
        sessionRepository.save(expiredSession);

        assertThatThrownBy(() -> consultationRequestService.getStatus(
                expiredSession.getId(), created.requestId()
        ))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_EXPIRED);
    }

    private void assertValidationError(CreateConsultationRequest request) {
        assertThatThrownBy(() -> consultationRequestService.create(session.getId(), request))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);
    }

    private CreateConsultationResponse createConsultationRequest(String sessionId) {
        return consultationRequestService.create(
                sessionId,
                new CreateConsultationRequest(
                        HelpType.PRODUCT_RECOMMENDATION,
                        List.of(),
                        false
                )
        );
    }

    private Product saveProduct(String id, String productCode) {
        Product product = Product.of(
                id,
                productCode,
                "상담 테스트 제품",
                "가죽",
                "테스트 특징",
                100_000L,
                List.of("M"),
                List.of("블랙"),
                "https://example.com/" + id + ".jpg",
                "테스트 브랜드 설명",
                null
        );
        return productRepository.save(product);
    }
}

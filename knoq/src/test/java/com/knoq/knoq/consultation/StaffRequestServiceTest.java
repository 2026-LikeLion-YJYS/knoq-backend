package com.knoq.knoq.consultation;

import com.knoq.knoq.consultation.dto.response.StaffRequestDetailResponse;
import com.knoq.knoq.consultation.dto.response.StaffRequestInboxResponse;
import com.knoq.knoq.consultation.dto.response.StaffRequestSummaryResponse;
import com.knoq.knoq.consultation.entity.ConsultationRequest;
import com.knoq.knoq.consultation.entity.HelpType;
import com.knoq.knoq.consultation.entity.RequestStatus;
import com.knoq.knoq.consultation.repository.ConsultationRequestRepository;
import com.knoq.knoq.consultation.service.StaffRequestService;
import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.needs.entity.NeedsAnalysis;
import com.knoq.knoq.needs.repository.NeedsAnalysisRepository;
import com.knoq.knoq.product.entity.Product;
import com.knoq.knoq.product.repository.ProductRepository;
import com.knoq.knoq.sessions.entity.LifestyleTag;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.repository.SessionRepository;
import com.knoq.knoq.staff.jwt.StaffTokenProvider;
import com.knoq.knoq.store.entity.Store;
import com.knoq.knoq.store.repository.StoreRepository;
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
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class StaffRequestServiceTest {

    @Autowired
    private StaffRequestService staffRequestService;

    @Autowired
    private StaffTokenProvider staffTokenProvider;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ConsultationRequestRepository consultationRequestRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private NeedsAnalysisRepository needsAnalysisRepository;

    private Store store;
    private Store anotherStore;
    private Session session;
    private String authorizationHeader;
    private String testSuffix;

    @BeforeEach
    void setUp() {
        testSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        store = storeRepository.save(Store.of("STAFF-" + testSuffix, "직원 테스트 매장"));
        anotherStore = storeRepository.save(Store.of("OTHER-" + testSuffix, "다른 테스트 매장"));

        session = saveSession("sess_staff_" + testSuffix, store.getId(), "노크고객");
        authorizationHeader = "Bearer " + staffTokenProvider.generateToken(
                "staff_sess_" + testSuffix,
                store.getStoreCode()
        );
    }

    @Test
    @DisplayName("직원 인박스는 해당 매장의 요청만 최신순으로 반환한다")
    void find_all_returns_only_store_requests_in_latest_order() {
        ConsultationRequest oldRequest = saveRequest(
                "req_old_" + testSuffix, session, HelpType.PRODUCT_INFO, false
        );
        ConsultationRequest newRequest = saveRequest(
                "req_new_" + testSuffix, session, HelpType.STYLING_RECOMMENDATION, false
        );

        Session anotherSession = saveSession(
                "sess_other_" + testSuffix, anotherStore.getId(), "다른고객"
        );
        saveRequest("req_other_" + testSuffix, anotherSession, HelpType.PRODUCT_RECOMMENDATION, false);

        StaffRequestInboxResponse response = staffRequestService.findAll(authorizationHeader);

        assertThat(response.requests())
                .extracting(StaffRequestSummaryResponse::requestId)
                .containsExactly(newRequest.getId(), oldRequest.getId());
        assertThat(response.requests())
                .allSatisfy(request -> {
                    assertThat(request.nickname()).isEqualTo("노크고객");
                    assertThat(request.lifestyleTags()).containsExactly(LifestyleTag.MINIMAL);
                    assertThat(request.status()).isEqualTo(RequestStatus.REQUESTED);
                });
    }

    @Test
    @DisplayName("직원 인박스는 상담 제품 개수를 반환한다")
    void find_all_returns_product_count() {
        Product productA = saveProduct("prod_staff_a_" + testSuffix, "STAFF-A-" + testSuffix);
        Product productB = saveProduct("prod_staff_b_" + testSuffix, "STAFF-B-" + testSuffix);
        ConsultationRequest request = ConsultationRequest.of(
                "req_products_" + testSuffix,
                session.getId(),
                store.getId(),
                HelpType.PRODUCT_COMPARISON,
                false
        );
        request.addProduct(productA.getId());
        request.addProduct(productB.getId());
        consultationRequestRepository.save(request);

        StaffRequestInboxResponse response = staffRequestService.findAll(authorizationHeader);

        assertThat(response.requests()).singleElement()
                .satisfies(summary -> assertThat(summary.productCount()).isEqualTo(2));
    }

    @Test
    @DisplayName("직원 요청 상세는 제품과 동의한 니즈 분석을 반환한다")
    void find_detail_returns_products_and_consented_needs_analysis() {
        Product product = saveProduct("prod_detail_" + testSuffix, "DETAIL-" + testSuffix);
        ConsultationRequest request = ConsultationRequest.of(
                "req_detail_" + testSuffix,
                session.getId(),
                store.getId(),
                HelpType.PRODUCT_INFO,
                true
        );
        request.addProduct(product.getId());
        consultationRequestRepository.save(request);

        NeedsAnalysis needsAnalysis = NeedsAnalysis.of(session.getId());
        needsAnalysis.updateResult("가방", "블랙", "가죽", "M", "가죽 소재를 선호합니다.");
        needsAnalysisRepository.save(needsAnalysis);

        StaffRequestDetailResponse response =
                staffRequestService.findDetail(authorizationHeader, request.getId());

        assertThat(response.requestId()).isEqualTo(request.getId());
        assertThat(response.nickname()).isEqualTo("노크고객");
        assertThat(response.products()).singleElement()
                .satisfies(detail -> assertThat(detail.productId()).isEqualTo(product.getId()));
        assertThat(response.needsAnalysis()).isNotNull();
        assertThat(response.needsAnalysis().getPreferredMaterial()).isEqualTo("가죽");
    }

    @Test
    @DisplayName("니즈 분석 전달에 동의하지 않으면 상세에 니즈 분석을 포함하지 않는다")
    void find_detail_excludes_needs_analysis_without_consent() {
        ConsultationRequest request = saveRequest(
                "req_no_needs_" + testSuffix, session, HelpType.PRODUCT_RECOMMENDATION, false
        );
        NeedsAnalysis needsAnalysis = NeedsAnalysis.of(session.getId());
        needsAnalysis.updateResult("가방", "블랙", "가죽", "M", "가죽 소재를 선호합니다.");
        needsAnalysisRepository.save(needsAnalysis);

        StaffRequestDetailResponse response =
                staffRequestService.findDetail(authorizationHeader, request.getId());

        assertThat(response.needsAnalysis()).isNull();
    }

    @Test
    @DisplayName("다른 매장의 상담 요청 상세는 조회할 수 없다")
    void find_detail_fails_when_request_belongs_to_another_store() {
        Session anotherSession = saveSession(
                "sess_detail_other_" + testSuffix, anotherStore.getId(), "다른고객"
        );
        ConsultationRequest request = saveRequest(
                "req_detail_other_" + testSuffix,
                anotherSession,
                HelpType.PRODUCT_INFO,
                false
        );

        assertThatThrownBy(() -> staffRequestService.findDetail(authorizationHeader, request.getId()))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONSULTATION_REQUEST_NOT_FOUND);
    }

    @Test
    @DisplayName("직원 인증 헤더가 없거나 토큰이 잘못되면 401 예외가 발생한다")
    void authentication_fails_with_invalid_authorization() {
        assertThatThrownBy(() -> staffRequestService.findAll(null))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);

        assertThatThrownBy(() -> staffRequestService.findAll("Bearer invalid-token"))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("직원 요청 응답에는 전달 금지 개인정보 필드가 없다")
    void responses_do_not_contain_prohibited_personal_information() {
        Set<String> summaryFields = fieldNamesOf(StaffRequestSummaryResponse.class);
        Set<String> detailFields = fieldNamesOf(StaffRequestDetailResponse.class);

        assertThat(summaryFields).doesNotContain(
                "realName", "face", "contact", "budget", "purchaseHistory", "preciseLocation"
        );
        assertThat(detailFields).doesNotContain(
                "realName", "face", "contact", "budget", "purchaseHistory", "preciseLocation"
        );
    }

    private Session saveSession(String id, Long storeId, String nickname) {
        Session savedSession = Session.of(
                id,
                "token_" + id,
                storeId,
                LocalDateTime.now().plusMinutes(30)
        );
        savedSession.updateNickname(nickname);
        savedSession.updateLifestyleTags(List.of(LifestyleTag.MINIMAL));
        return sessionRepository.save(savedSession);
    }

    private ConsultationRequest saveRequest(String id, Session owner, HelpType helpType,
                                            boolean includeNeedsAnalysis) {
        ConsultationRequest request = ConsultationRequest.of(
                id,
                owner.getId(),
                owner.getStoreId(),
                helpType,
                includeNeedsAnalysis
        );
        return consultationRequestRepository.saveAndFlush(request);
    }

    private Product saveProduct(String id, String productCode) {
        Product product = Product.of(
                id,
                productCode,
                "직원 요청 테스트 제품",
                "가죽",
                "테스트 특징",
                100_000L,
                List.of("M"),
                List.of("블랙"),
                "https://example.com/" + id + ".jpg",
                "브랜드 설명",
                "AI 제품 설명"
        );
        return productRepository.save(product);
    }

    private Set<String> fieldNamesOf(Class<?> responseType) {
        return Arrays.stream(responseType.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
    }
}

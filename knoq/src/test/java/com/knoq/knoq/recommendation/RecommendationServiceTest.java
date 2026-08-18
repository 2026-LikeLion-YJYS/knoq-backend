package com.knoq.knoq.recommendation;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.product.entity.Product;
import com.knoq.knoq.product.repository.ProductRepository;
import com.knoq.knoq.recommendation.dto.response.RecommendationResponse;
import com.knoq.knoq.recommendation.service.RecommendationService;
import com.knoq.knoq.saved.entity.SavedProductSource;
import com.knoq.knoq.saved.repository.SavedProductRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class RecommendationServiceTest {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SavedProductRepository savedProductRepository;

    @MockitoBean
    private ProductRepository productRepository;

    private Session session;
    private List<Product> products;
    private String testSuffix;

    @BeforeEach
    void setUp() {
        testSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        session = Session.of(
                "sess_recommend_" + testSuffix,
                "token_recommend_" + testSuffix,
                1L,
                LocalDateTime.now().plusMinutes(30)
        );
        session.updateLifestyleTags(List.of(LifestyleTag.MINIMAL));
        sessionRepository.save(session);

        products = List.of(
                product("prod_minimal_a_" + testSuffix, "REC-A-" + testSuffix,
                        "미니멀 백", "가죽", "심플하고 베이직한 블랙 디자인"),
                product("prod_minimal_b_" + testSuffix, "REC-B-" + testSuffix,
                        "베이직 파우치", "캔버스", "화이트 무지 디자인"),
                product("prod_minimal_c_" + testSuffix, "REC-C-" + testSuffix,
                        "심플 지갑", "레더", "그레이 컬러의 미니멀 디자인"),
                product("prod_casual_" + testSuffix, "REC-D-" + testSuffix,
                        "캐주얼 백팩", "나일론", "편안한 데일리 제품")
        );
        when(productRepository.findAll()).thenReturn(products);
    }

    @Test
    @DisplayName("라이프스타일 태그와 제품 속성을 비교해 점수가 높은 제품 3개를 추천한다")
    void recommend_returns_top_three_products_by_lifestyle_rule() {
        RecommendationResponse response = recommendationService.recommend(session.getId());

        assertThat(response.products()).hasSize(3);
        assertThat(response.products())
                .extracting(product -> product.productId())
                .containsExactly(
                        products.get(0).getId(),
                        products.get(1).getId(),
                        products.get(2).getId()
                );
        assertThat(response.summary()).contains("미니멀", "3개");
        assertThat(response.products())
                .allSatisfy(product -> assertThat(product.reason()).contains("미니멀"));
    }

    @Test
    @DisplayName("추천 제품은 RECOMMEND 출처로 보관함에 자동 저장된다")
    void recommend_saves_products_with_recommend_source() {
        RecommendationResponse response = recommendationService.recommend(session.getId());

        assertThat(savedProductRepository.findBySessionIdOrderBySavedAtDesc(session.getId()))
                .hasSize(3)
                .allSatisfy(savedProduct ->
                        assertThat(savedProduct.getSource()).isEqualTo(SavedProductSource.RECOMMEND));
        assertThat(response.products())
                .allSatisfy(product -> assertThat(product.savedProductId()).startsWith("sav_"));
    }

    @Test
    @DisplayName("추천을 다시 요청해도 같은 제품은 보관함에 중복 저장되지 않는다")
    void repeated_recommendation_does_not_duplicate_saved_products() {
        RecommendationResponse first = recommendationService.recommend(session.getId());
        RecommendationResponse second = recommendationService.recommend(session.getId());

        assertThat(savedProductRepository.countBySessionId(session.getId())).isEqualTo(3);
        assertThat(second.products())
                .extracting(product -> product.savedProductId())
                .containsExactlyElementsOf(
                        first.products().stream().map(product -> product.savedProductId()).toList()
                );
    }

    @Test
    @DisplayName("추천 결과에는 같은 제품이 중복으로 포함되지 않는다")
    void recommendation_does_not_contain_duplicate_products() {
        RecommendationResponse response = recommendationService.recommend(session.getId());

        assertThat(response.products())
                .extracting(product -> product.productId())
                .doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("만료된 세션으로 추천을 요청하면 410 예외가 발생한다")
    void recommend_fails_when_session_is_expired() {
        Session expiredSession = Session.of(
                "sess_recommend_expired_" + testSuffix,
                "token_recommend_expired_" + testSuffix,
                1L,
                LocalDateTime.now().minusMinutes(1)
        );
        expiredSession.updateLifestyleTags(List.of(LifestyleTag.MINIMAL));
        sessionRepository.save(expiredSession);

        assertThatThrownBy(() -> recommendationService.recommend(expiredSession.getId()))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_EXPIRED);
    }

    @Test
    @DisplayName("존재하지 않는 세션으로 추천을 요청하면 404 예외가 발생한다")
    void recommend_fails_when_session_does_not_exist() {
        assertThatThrownBy(() -> recommendationService.recommend("sess_not_exist_" + testSuffix))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_FOUND);
    }

    private Product product(String id, String productCode, String name, String material, String features) {
        Product product = Product.of(
                id,
                productCode,
                name,
                material,
                features,
                100_000L,
                List.of("M"),
                List.of("블랙"),
                "https://example.com/" + id + ".jpg",
                "브랜드 설명",
                null
        );
        product.updateCategory("가방");
        return product;
    }
}

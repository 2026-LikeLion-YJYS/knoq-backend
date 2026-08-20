package com.knoq.knoq.needs;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.needs.dto.request.UpdateNeedsAnalysisRequest;
import com.knoq.knoq.needs.dto.response.NeedsAnalysisResponse;
import com.knoq.knoq.needs.dto.response.NeedsAnalysisResultResponse;
import com.knoq.knoq.needs.entity.NeedsAnalysis;
import com.knoq.knoq.needs.repository.NeedsAnalysisRepository;
import com.knoq.knoq.needs.service.NeedsAnalysisService;
import com.knoq.knoq.needs.service.NeedsCommentGenerator;
import com.knoq.knoq.product.entity.Product;
import com.knoq.knoq.product.repository.ProductRepository;
import com.knoq.knoq.saved.entity.SavedProduct;
import com.knoq.knoq.saved.entity.SavedProductSource;
import com.knoq.knoq.saved.repository.SavedProductRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class NeedsAnalysisServiceTest {

    @Autowired
    private NeedsAnalysisService needsAnalysisService;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SavedProductRepository savedProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private NeedsAnalysisRepository needsAnalysisRepository;

    @MockitoBean
    private NeedsCommentGenerator needsCommentGenerator;

    private Session session;

    @BeforeEach
    void setUp() {
        session = Session.of(
                "sess_needs_test",
                "token_needs_test",
                1L,
                LocalDateTime.now().plusMinutes(30)
        );

        sessionRepository.save(session);
    }

    @Test
    @DisplayName("저장한 제품이 2개 미만이면 canAnalyze는 false다")
    void canAnalyze_false_when_saved_count_under_2() {
        savedProductRepository.save(
                SavedProduct.of(
                        session.getId(),
                        "prod_1",
                        SavedProductSource.CAMERA
                )
        );

        NeedsAnalysisResponse response =
                needsAnalysisService.getAnalysis(session.getId());

        assertThat(response.isCanAnalyze()).isFalse();
        assertThat(response.getSavedCount()).isEqualTo(1);
        assertThat(response.getAnalysis()).isNull();
    }

    @Test
    @DisplayName("저장한 제품이 2개 미만이면 분석 요청 시 400 예외가 발생한다")
    void analyze_fails_when_not_enough_saved_products() {
        savedProductRepository.save(
                SavedProduct.of(
                        session.getId(),
                        "prod_1",
                        SavedProductSource.CAMERA
                )
        );

        assertThatThrownBy(
                () -> needsAnalysisService.analyze(session.getId())
        )
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.NEEDS_ANALYSIS_NOT_ENOUGH_SAVED_PRODUCTS
                );
    }

    @Test
    @DisplayName("저장한 제품이 2개 이상이면 분석 결과가 저장되고 조회에도 반영된다")
    void analyze_and_then_get_reflects_result() {
        Product productA = createProduct(
                "prod_needs_1",
                "PD-NEEDS-1",
                "울 니트 A",
                "비세토스 모노그램 캔버스 + 나파 가죽",
                List.of("M"),
                List.of("블랙")
        );

        Product productB = createProduct(
                "prod_needs_2",
                "PD-NEEDS-2",
                "울 니트 B",
                "비세토스 모노그램 캔버스 + 나파 가죽",
                List.of("M"),
                List.of("블랙")
        );

        productRepository.saveAll(List.of(productA, productB));

        savedProductRepository.save(
                SavedProduct.of(
                        session.getId(),
                        productA.getId(),
                        SavedProductSource.CAMERA
                )
        );

        savedProductRepository.save(
                SavedProduct.of(
                        session.getId(),
                        productB.getId(),
                        SavedProductSource.RECOMMEND
                )
        );

        NeedsAnalysisResultResponse result =
                needsAnalysisService.analyze(session.getId());

        assertThat(result.getProductCategory()).isEqualTo("상의");
        assertThat(result.getPreferredMaterial()).isEqualTo("Visetos");
        assertThat(result.getPreferredColor()).isEqualTo("블랙");
        assertThat(result.getPreferredSize()).isEqualTo("M");
        assertThat(result.getComment()).isNotBlank();
        assertThat(result.getAnalyzedAt()).isNotNull();

        NeedsAnalysisResponse response =
                needsAnalysisService.getAnalysis(session.getId());

        assertThat(response.isCanAnalyze()).isTrue();
        assertThat(response.getSavedCount()).isEqualTo(2);
        assertThat(response.getAnalysis()).isNotNull();
        assertThat(response.getAnalysis().getProductCategory())
                .isEqualTo("상의");
        assertThat(response.getAnalysis().getPreferredMaterial())
                .isEqualTo("Visetos");
        assertThat(response.getAnalysis().getPreferredColor())
                .isEqualTo("블랙");
        assertThat(response.getAnalysis().getPreferredSize())
                .isEqualTo("M");

        assertThat(
                needsAnalysisRepository.findBySessionId(session.getId())
        ).isPresent();
    }

    @Test
    @DisplayName("코멘트 생성기가 문장을 반환하면 그 문장을 그대로 사용한다")
    void analyze_uses_generated_comment_when_available() {
        Product productA = createProduct(
                "prod_comment_1",
                "PD-COMMENT-1",
                "울 니트 A",
                "나파 가죽",
                List.of("M"),
                List.of("블랙")
        );

        Product productB = createProduct(
                "prod_comment_2",
                "PD-COMMENT-2",
                "울 니트 B",
                "나파 가죽",
                List.of("M"),
                List.of("블랙")
        );

        productRepository.saveAll(List.of(productA, productB));
        saveProduct(productA, SavedProductSource.CAMERA);
        saveProduct(productB, SavedProductSource.RECOMMEND);

        when(needsCommentGenerator.generate(any()))
                .thenReturn("블랙 컬러의 울 소재 M 사이즈를 눈여겨보고 계시네요.");

        NeedsAnalysisResultResponse result = needsAnalysisService.analyze(session.getId());

        assertThat(result.getComment()).isEqualTo("블랙 컬러의 울 소재 M 사이즈를 눈여겨보고 계시네요.");
    }

    @Test
    @DisplayName("코멘트 생성기가 실패(null)하면 룰 기반 문장으로 대체된다")
    void analyze_falls_back_to_template_comment_when_generator_fails() {
        Product productA = createProduct(
                "prod_fallback_1",
                "PD-FALLBACK-1",
                "울 니트 A",
                "나파 가죽",
                List.of("M"),
                List.of("블랙")
        );

        Product productB = createProduct(
                "prod_fallback_2",
                "PD-FALLBACK-2",
                "울 니트 B",
                "나파 가죽",
                List.of("M"),
                List.of("블랙")
        );

        productRepository.saveAll(List.of(productA, productB));
        saveProduct(productA, SavedProductSource.CAMERA);
        saveProduct(productB, SavedProductSource.RECOMMEND);

        when(needsCommentGenerator.generate(any()))
                .thenReturn(null);

        NeedsAnalysisResultResponse result = needsAnalysisService.analyze(session.getId());

        assertThat(result.getComment())
                .isEqualTo("저장하신 제품들은 주로 Leather 소재, 블랙 계열, M 사이즈를 선호하시는 경향이 있습니다.");
    }

    @Test
    @DisplayName("재분석하면 기존 분석 행이 갱신되고 새로 생성되지 않는다")
    void reanalyze_updates_existing_row() {
        Product woolProductA = createProduct(
                "prod_wool_1",
                "PD-WOOL-1",
                "울 제품 A",
                "비세토스 캔버스",
                List.of("M"),
                List.of("블랙")
        );

        Product woolProductB = createProduct(
                "prod_wool_2",
                "PD-WOOL-2",
                "울 제품 B",
                "비세토스 캔버스",
                List.of("M"),
                List.of("블랙")
        );

        Product cottonProductA = createProduct(
                "prod_cotton_1",
                "PD-COTTON-1",
                "면 제품 A",
                "나일론 100%",
                List.of("L"),
                List.of("화이트")
        );

        Product cottonProductB = createProduct(
                "prod_cotton_2",
                "PD-COTTON-2",
                "면 제품 B",
                "나일론 100%",
                List.of("L"),
                List.of("화이트")
        );

        productRepository.saveAll(
                List.of(
                        woolProductA,
                        woolProductB,
                        cottonProductA,
                        cottonProductB
                )
        );

        saveProduct(woolProductA, SavedProductSource.CAMERA);
        saveProduct(woolProductB, SavedProductSource.RECOMMEND);

        NeedsAnalysisResultResponse firstResult =
                needsAnalysisService.analyze(session.getId());

        assertThat(firstResult.getPreferredMaterial()).isEqualTo("Visetos");
        assertThat(firstResult.getPreferredColor()).isEqualTo("블랙");
        assertThat(firstResult.getPreferredSize()).isEqualTo("M");
        assertThat(needsAnalysisRepository.count()).isEqualTo(1);

        savedProductRepository.deleteBySessionId(session.getId());

        saveProduct(cottonProductA, SavedProductSource.CAMERA);
        saveProduct(cottonProductB, SavedProductSource.RECOMMEND);

        NeedsAnalysisResultResponse secondResult =
                needsAnalysisService.analyze(session.getId());

        assertThat(secondResult.getPreferredMaterial()).isEqualTo("Nylon");
        assertThat(secondResult.getPreferredColor()).isEqualTo("화이트");
        assertThat(secondResult.getPreferredSize()).isEqualTo("L");

        assertThat(needsAnalysisRepository.count()).isEqualTo(1);

        assertThat(
                needsAnalysisRepository
                        .findBySessionId(session.getId())
                        .orElseThrow()
                        .getPreferredMaterial()
        ).isEqualTo("Nylon");
    }

    @Test
    @DisplayName("사용자가 니즈 항목을 수정하면 결과가 저장되고 기존 코멘트와 분석 시각은 유지된다")
    void update_analysis_saves_user_selections_and_preserves_analysis_metadata() {
        NeedsAnalysis needsAnalysis = NeedsAnalysis.of(session.getId());
        needsAnalysis.updateResult("가방", "블랙", "가죽", "M", "기존 KNOQ'S 발견 문구");
        needsAnalysisRepository.save(needsAnalysis);

        LocalDateTime originalAnalyzedAt = needsAnalysis.getAnalyzedAt();
        LocalDateTime originalExpiresAt = session.getExpiresAt();

        NeedsAnalysisResultResponse result = needsAnalysisService.updateAnalysis(
                session.getId(),
                new UpdateNeedsAnalysisRequest(
                        "토트백 / 쇼퍼백",
                        "Black · Cognac",
                        "Leather",
                        "Medium · Large"
                )
        );

        assertThat(result.getProductCategory()).isEqualTo("토트백 / 쇼퍼백");
        assertThat(result.getPreferredColor()).isEqualTo("Black · Cognac");
        assertThat(result.getPreferredMaterial()).isEqualTo("Leather");
        assertThat(result.getPreferredSize()).isEqualTo("Medium · Large");
        assertThat(result.getComment()).isEqualTo("기존 KNOQ'S 발견 문구");
        assertThat(result.getAnalyzedAt()).isEqualTo(originalAnalyzedAt);
        assertThat(sessionRepository.findById(session.getId()).orElseThrow().getExpiresAt())
                .isAfter(originalExpiresAt);

        NeedsAnalysisResponse getResponse = needsAnalysisService.getAnalysis(session.getId());
        assertThat(getResponse.getAnalysis().getProductCategory()).isEqualTo("토트백 / 쇼퍼백");
        assertThat(getResponse.getAnalysis().getPreferredColor()).isEqualTo("Black · Cognac");
        assertThat(getResponse.getAnalysis().getPreferredMaterial()).isEqualTo("Leather");
        assertThat(getResponse.getAnalysis().getPreferredSize()).isEqualTo("Medium · Large");
        assertThat(getResponse.getAnalysis().getComment()).isEqualTo("기존 KNOQ'S 발견 문구");
    }

    @Test
    @DisplayName("기존 니즈 분석이 없으면 수정 요청 시 404 예외가 발생한다")
    void update_analysis_fails_when_analysis_does_not_exist() {
        UpdateNeedsAnalysisRequest request = new UpdateNeedsAnalysisRequest(
                "가방", "블랙", "가죽", "M"
        );

        assertThatThrownBy(() -> needsAnalysisService.updateAnalysis(session.getId(), request))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("만료된 세션은 니즈 분석 결과를 수정할 수 없다")
    void update_analysis_fails_when_session_is_expired() {
        Session expiredSession = Session.of(
                "sess_needs_expired",
                "token_needs_expired",
                1L,
                LocalDateTime.now().minusMinutes(1)
        );
        sessionRepository.save(expiredSession);

        NeedsAnalysis needsAnalysis = NeedsAnalysis.of(expiredSession.getId());
        needsAnalysis.updateResult("가방", "블랙", "가죽", "M", "기존 문구");
        needsAnalysisRepository.save(needsAnalysis);

        UpdateNeedsAnalysisRequest request = new UpdateNeedsAnalysisRequest(
                "토트백", "브라운", "캔버스", "L"
        );

        assertThatThrownBy(() -> needsAnalysisService.updateAnalysis(expiredSession.getId(), request))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_EXPIRED);
    }

    private Product createProduct(
            String id,
            String productCode,
            String name,
            String material,
            List<String> sizes,
            List<String> colors
    ) {
        Product product = Product.of(
                id,
                productCode,
                name,
                material,
                "테스트 제품 특징",
                100_000L,
                sizes,
                colors,
                "https://example.com/" + id + ".jpg",
                "테스트 브랜드 설명",
                null
        );
        product.updateCategory("상의");
        return product;
    }

    private void saveProduct(
            Product product,
            SavedProductSource source
    ) {
        savedProductRepository.save(
                SavedProduct.of(
                        session.getId(),
                        product.getId(),
                        source
                )
        );
    }
}

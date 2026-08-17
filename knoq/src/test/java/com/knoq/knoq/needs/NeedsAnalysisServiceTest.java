package com.knoq.knoq.needs;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.needs.dto.response.NeedsAnalysisResponse;
import com.knoq.knoq.needs.dto.response.NeedsAnalysisResultResponse;
import com.knoq.knoq.needs.repository.NeedsAnalysisRepository;
import com.knoq.knoq.needs.service.NeedsAnalysisService;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                "울",
                List.of("M"),
                List.of("블랙")
        );

        Product productB = createProduct(
                "prod_needs_2",
                "PD-NEEDS-2",
                "울 니트 B",
                "울",
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

        assertThat(result.getProductCategory()).isNull();
        assertThat(result.getPreferredMaterial()).isEqualTo("울");
        assertThat(result.getPreferredColor()).isEqualTo("블랙");
        assertThat(result.getPreferredSize()).isEqualTo("M");
        assertThat(result.getComment()).isNotBlank();
        assertThat(result.getAnalyzedAt()).isNotNull();

        NeedsAnalysisResponse response =
                needsAnalysisService.getAnalysis(session.getId());

        assertThat(response.isCanAnalyze()).isTrue();
        assertThat(response.getSavedCount()).isEqualTo(2);
        assertThat(response.getAnalysis()).isNotNull();
        assertThat(response.getAnalysis().getPreferredMaterial())
                .isEqualTo("울");
        assertThat(response.getAnalysis().getPreferredColor())
                .isEqualTo("블랙");
        assertThat(response.getAnalysis().getPreferredSize())
                .isEqualTo("M");

        assertThat(
                needsAnalysisRepository.findBySessionId(session.getId())
        ).isPresent();
    }

    @Test
    @DisplayName("재분석하면 기존 분석 행이 갱신되고 새로 생성되지 않는다")
    void reanalyze_updates_existing_row() {
        Product woolProductA = createProduct(
                "prod_wool_1",
                "PD-WOOL-1",
                "울 제품 A",
                "울",
                List.of("M"),
                List.of("블랙")
        );

        Product woolProductB = createProduct(
                "prod_wool_2",
                "PD-WOOL-2",
                "울 제품 B",
                "울",
                List.of("M"),
                List.of("블랙")
        );

        Product cottonProductA = createProduct(
                "prod_cotton_1",
                "PD-COTTON-1",
                "면 제품 A",
                "면",
                List.of("L"),
                List.of("화이트")
        );

        Product cottonProductB = createProduct(
                "prod_cotton_2",
                "PD-COTTON-2",
                "면 제품 B",
                "면",
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

        assertThat(firstResult.getPreferredMaterial()).isEqualTo("울");
        assertThat(firstResult.getPreferredColor()).isEqualTo("블랙");
        assertThat(firstResult.getPreferredSize()).isEqualTo("M");
        assertThat(needsAnalysisRepository.count()).isEqualTo(1);

        savedProductRepository.deleteBySessionId(session.getId());

        saveProduct(cottonProductA, SavedProductSource.CAMERA);
        saveProduct(cottonProductB, SavedProductSource.RECOMMEND);

        NeedsAnalysisResultResponse secondResult =
                needsAnalysisService.analyze(session.getId());

        assertThat(secondResult.getPreferredMaterial()).isEqualTo("면");
        assertThat(secondResult.getPreferredColor()).isEqualTo("화이트");
        assertThat(secondResult.getPreferredSize()).isEqualTo("L");

        assertThat(needsAnalysisRepository.count()).isEqualTo(1);

        assertThat(
                needsAnalysisRepository
                        .findBySessionId(session.getId())
                        .orElseThrow()
                        .getPreferredMaterial()
        ).isEqualTo("면");
    }

    private Product createProduct(
            String id,
            String productCode,
            String name,
            String material,
            List<String> sizes,
            List<String> colors
    ) {
        return Product.of(
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
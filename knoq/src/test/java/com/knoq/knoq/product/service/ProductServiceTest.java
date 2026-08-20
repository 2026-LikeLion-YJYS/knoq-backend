package com.knoq.knoq.product.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.product.dto.ProductDetailResponse;
import com.knoq.knoq.product.entity.Product;
import com.knoq.knoq.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Test
    @DisplayName("존재하는 제품 ID로 조회하면 상세 정보를 반환한다")
    void getProductDetail_success() {
        // given
        Product product = Product.of(
                "prod_test1", "PD-9999", "테스트 니트", "울 100%", "가볍고 신축성이 좋습니다", 89000L,
                List.of("S", "M", "L"), List.of("블랙", "베이지"), "https://example.com/thumb.jpg",
                "브랜드 공식 설명입니다.", null
        );
        product.updateStructuredFeatures(
                List.of("캐주얼"),
                List.of("리브 조직"),
                List.of("데일리 착용")
        );
        product.addReferenceImage("front-base64");
        product.addReferenceImage("side-base64");
        product.addReferenceImage("top-base64");
        when(productRepository.findById("prod_test1")).thenReturn(Optional.of(product));

        // when
        ProductDetailResponse response = productService.getProductDetail("prod_test1");

        // then
        assertThat(response.productId()).isEqualTo("prod_test1");
        assertThat(response.name()).isEqualTo("테스트 니트");
        assertThat(response.material()).isEqualTo("울 100%");
        assertThat(response.features().style()).containsExactly("캐주얼");
        assertThat(response.features().styleImageUrl()).isNull();
        assertThat(response.features().composition()).containsExactly("리브 조직");
        assertThat(response.features().compositionImageUrl()).isNull();
        assertThat(response.features().usage()).containsExactly("데일리 착용");
        assertThat(response.price()).isEqualTo(89000L);
        assertThat(response.size()).containsExactly("S", "M", "L");
        assertThat(response.color()).containsExactly("블랙", "베이지");
        assertThat(response.thumbnailUrl()).isEqualTo("https://example.com/thumb.jpg");
        assertThat(response.images()).isEmpty();
        assertThat(response.aiGenerated()).isNull();
    }

    @Test
    @DisplayName("material이 비어있으면 기본 문구를, 구조화된 특징이 없으면 빈 목록을 반환한다")
    void getProductDetail_missingInfo_returnsDefaultText() {
        // given
        Product product = Product.of(
                "prod_test2", "PD-9998", "테스트 티셔츠", null, null, 29000L,
                List.of("FREE"), List.of("화이트"), null,
                null, null
        );
        when(productRepository.findById("prod_test2")).thenReturn(Optional.of(product));

        // when
        ProductDetailResponse response = productService.getProductDetail("prod_test2");

        // then
        assertThat(response.material()).isEqualTo("정보 없음");
        assertThat(response.features().style()).isEmpty();
        assertThat(response.features().composition()).isEmpty();
        assertThat(response.features().usage()).isEmpty();
        assertThat(response.thumbnailUrl()).isNull();
        assertThat(response.images()).isEmpty();
    }

    @Test
    @DisplayName("데모 제품은 스타일과 구성 이미지 URL을 반환한다")
    void getProductDetail_demoProduct_returnsFeatureImageUrls() {
        Product product = Product.of(
                "prod_1", "PD-DEMO-1", "데모 가방", "가죽", "특징", 100_000L,
                List.of("L"), List.of("Cognac"), "/demo/products/prod_1/front.png",
                "브랜드 설명", "AI 설명"
        );
        when(productRepository.findById("prod_1")).thenReturn(Optional.of(product));

        ProductDetailResponse response = productService.getProductDetail("prod_1");

        assertThat(response.features().styleImageUrl())
                .isEqualTo("/demo/products/prod_1/style.png");
        assertThat(response.features().compositionImageUrl())
                .isEqualTo("/demo/products/prod_1/composition.png");
        assertThat(response.images()).containsExactly(
                "/demo/products/prod_1/front.png",
                "/demo/products/prod_1/side.png",
                "/demo/products/prod_1/top.png",
                "/demo/products/prod_1/detail.png"
        );
    }

    @Test
    @DisplayName("존재하지 않는 제품 ID로 조회하면 PRODUCT_NOT_FOUND 예외가 발생한다")
    void getProductDetail_notFound_throwsException() {
        when(productRepository.findById("prod_not_exist")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductDetail("prod_not_exist"))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
    }
}

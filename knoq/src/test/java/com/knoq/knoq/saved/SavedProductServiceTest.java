package com.knoq.knoq.saved;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.product.repository.ProductRepository;
import com.knoq.knoq.saved.entity.SavedProduct;
import com.knoq.knoq.saved.entity.SavedProductSource;
import com.knoq.knoq.saved.repository.SavedProductRepository;
import com.knoq.knoq.saved.service.SavedProductService;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.service.SessionExpirationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavedProductServiceTest {

    @Mock
    private SavedProductRepository savedProductRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SessionExpirationService sessionExpirationService;

    @InjectMocks
    private SavedProductService savedProductService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(savedProductService, "maxSavedProducts", 9);
        when(sessionExpirationService.getValidSessionAndRefresh("sess_test"))
                .thenReturn(mock(Session.class));
    }

    @Test
    @DisplayName("카메라에서 확인한 실제 제품을 CAMERA 출처로 저장한다")
    void saveFromCamera_saves_with_camera_source() {
        prepareNewProduct("prod_1", 0);
        when(savedProductRepository.save(any(SavedProduct.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SavedProduct result = savedProductService.saveFromCamera("sess_test", "prod_1");

        assertThat(result.getProductId()).isEqualTo("prod_1");
        assertThat(result.getSource()).isEqualTo(SavedProductSource.CAMERA);
    }

    @Test
    @DisplayName("추천 제품을 RECOMMEND 출처로 저장한다")
    void saveFromRecommend_saves_with_recommend_source() {
        prepareNewProduct("prod_2", 0);
        when(savedProductRepository.save(any(SavedProduct.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SavedProduct result = savedProductService.saveFromRecommend("sess_test", "prod_2");

        assertThat(result.getProductId()).isEqualTo("prod_2");
        assertThat(result.getSource()).isEqualTo(SavedProductSource.RECOMMEND);
    }

    @Test
    @DisplayName("존재하지 않는 제품은 보관함에 저장할 수 없다")
    void save_fails_when_product_does_not_exist() {
        when(productRepository.existsById("prod_missing")).thenReturn(false);

        assertThatThrownBy(() ->
                savedProductService.saveFromCamera("sess_test", "prod_missing")
        )
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);

        verify(savedProductRepository, never()).save(any(SavedProduct.class));
    }

    @Test
    @DisplayName("8개가 저장된 상태에서는 9번째 제품을 저장할 수 있다")
    void save_allows_ninth_product() {
        prepareNewProduct("prod_9", 8);
        when(savedProductRepository.save(any(SavedProduct.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SavedProduct result = savedProductService.saveFromCamera("sess_test", "prod_9");

        assertThat(result.getProductId()).isEqualTo("prod_9");
        verify(savedProductRepository).save(any(SavedProduct.class));
    }

    @Test
    @DisplayName("9개가 저장된 상태에서는 추천 제품을 추가로 저장할 수 없다")
    void saveFromRecommend_fails_when_limit_is_reached() {
        prepareNewProduct("prod_10", 9);

        assertThatThrownBy(() ->
                savedProductService.saveFromRecommend("sess_test", "prod_10")
        )
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SAVED_PRODUCT_LIMIT_EXCEEDED);

        verify(savedProductRepository, never()).save(any(SavedProduct.class));
    }

    @Test
    @DisplayName("저장 상한에 도달해도 이미 저장된 제품은 기존 결과를 반환한다")
    void duplicate_returns_existing_product_even_when_limit_is_reached() {
        SavedProduct existing = SavedProduct.of(
                "sess_test",
                "prod_1",
                SavedProductSource.CAMERA
        );
        when(productRepository.existsById("prod_1")).thenReturn(true);
        when(savedProductRepository.findBySessionIdAndProductId("sess_test", "prod_1"))
                .thenReturn(Optional.of(existing));

        SavedProduct result = savedProductService.saveFromRecommend("sess_test", "prod_1");

        assertThat(result).isSameAs(existing);
        verify(savedProductRepository, never()).countBySessionId("sess_test");
        verify(savedProductRepository, never()).save(any(SavedProduct.class));
    }

    private void prepareNewProduct(String productId, long savedCount) {
        when(productRepository.existsById(productId)).thenReturn(true);
        when(savedProductRepository.findBySessionIdAndProductId("sess_test", productId))
                .thenReturn(Optional.empty());
        when(savedProductRepository.countBySessionId("sess_test")).thenReturn(savedCount);
    }
}

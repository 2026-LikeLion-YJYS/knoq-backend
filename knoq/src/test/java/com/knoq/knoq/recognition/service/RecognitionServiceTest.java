package com.knoq.knoq.recognition.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.product.entity.Product;
import com.knoq.knoq.product.repository.ProductRepository;
import com.knoq.knoq.recognition.client.OpenAiVisionClient;
import com.knoq.knoq.recognition.dto.ConfirmRecognitionRequest;
import com.knoq.knoq.recognition.dto.ConfirmRecognitionResponse;
import com.knoq.knoq.recognition.dto.ProductLookupRequest;
import com.knoq.knoq.recognition.dto.ProductLookupResponse;
import com.knoq.knoq.recognition.dto.RecognitionResponse;
import com.knoq.knoq.saved.repository.SavedProductRepository;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.repository.SessionRepository;
import com.knoq.knoq.store.entity.Store;
import com.knoq.knoq.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class RecognitionServiceTest {

    @Autowired
    private RecognitionService recognitionService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ProductRepository productRepository;

    // 진짜 OpenAI에 요청 안 보내고 가짜 결과를 돌려주게 함
    @MockitoBean
    private OpenAiVisionClient openAiVisionClient;

    @Autowired
    private SavedProductRepository savedProductRepository;

    private String sessionId;

    @BeforeEach
    void setUp() {
        Store store = storeRepository.save(Store.of("TEST-200", "테스트 매장"));
        Session session = Session.of("sess_reco_test", "token_reco_test", store.getId(),
                LocalDateTime.now().plusHours(1));
        sessionRepository.save(session);
        sessionId = session.getId();
    }

    // referenceImages가 비어있으면 "기준 사진 없음" 취급, 값이 있으면 아무 문자열이나 넣어도 됨(GPT 호출 자체를 mock하니까)
    private Product saveProduct(String id, String code, String name, List<String> referenceImages) {
        Product product = Product.of(id, code, name, "울 100%", "특징", 100000L,
                List.of("FREE"), List.of("블랙"), "https://example.com/" + id + ".jpg", "브랜드 설명", null);
        if (referenceImages != null) {
            referenceImages.forEach(product::addReferenceImage);
        }
        return productRepository.save(product);
    }

    private MultipartFile fakeImage() {
        return new MockMultipartFile("image", "test.jpg", "image/jpeg", "fake-image-bytes".getBytes());
    }

    @Test
    void 기준_사진이_없으면_mock_매칭으로_폴백한다() {
        saveProduct("prod_a", "PD-A", "제품A", List.of()); // 기준 사진 없음

        RecognitionResponse response = recognitionService.recognize(sessionId, fakeImage());

        assertThat(response.recognitionId()).startsWith("rec_");
        assertThat(response.candidates()).isNotEmpty();
        // 기준 사진이 없으니 GPT 비전 API 자체를 호출하지 않아야 함
        org.mockito.Mockito.verifyNoInteractions(openAiVisionClient);
    }

    @Test
    void GPT가_confidence_가장_높은_제품을_최우선_후보로_반환하면_그대로_1순위가_된다() {
        saveProduct("prod_close", "PD-1", "가까운 제품", List.of("fake-base64-1"));
        saveProduct("prod_far", "PD-2", "먼 제품", List.of("fake-base64-2"));
        saveProduct("prod_far2", "PD-3", "먼 제품2", List.of("fake-base64-3"));
        when(openAiVisionClient.recognize(any(), anyList())).thenReturn(List.of(
                new OpenAiVisionClient.VisionMatch("prod_close", 0.92),
                new OpenAiVisionClient.VisionMatch("prod_far", 0.3)
        ));

        RecognitionResponse response = recognitionService.recognize(sessionId, fakeImage());

        assertThat(response.candidates().get(0).productId()).isEqualTo("prod_close");
    }

    @Test
    void OpenAI_API가_실패하면_mock으로_폴백한다() {
        saveProduct("prod_a", "PD-A", "제품A", List.of("fake-base64"));
        when(openAiVisionClient.recognize(any(), anyList())).thenThrow(new ApiException(
                com.knoq.knoq.global.exception.ErrorCode.VISION_RECOGNITION_FAILED));

        RecognitionResponse response = recognitionService.recognize(sessionId, fakeImage());

        assertThat(response.candidates()).isNotEmpty();
    }

    @Test
    void 후보_안에_있는_제품으로_확정해도_보관함에_자동_저장하지_않는다() {
        saveProduct("prod_a", "PD-A", "제품A", List.of());
        RecognitionResponse recognized = recognitionService.recognize(sessionId, fakeImage());
        String productId = recognized.candidates().get(0).productId();

        ConfirmRecognitionResponse response = recognitionService.confirm(
                sessionId, recognized.recognitionId(), new ConfirmRecognitionRequest(productId, true));

        assertThat(response.productId()).isEqualTo(productId);
        assertThat(response.confirmed()).isTrue();
        assertThat(savedProductRepository.countBySessionId(sessionId)).isZero();
    }

    @Test
    void 후보에_없는_제품으로_확정하려하면_예외를_던진다() {
        saveProduct("prod_a", "PD-A", "제품A", List.of());
        RecognitionResponse recognized = recognitionService.recognize(sessionId, fakeImage());

        assertThatThrownBy(() -> recognitionService.confirm(
                sessionId, recognized.recognitionId(), new ConfirmRecognitionRequest("prod_not_candidate", true)))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void 다시_촬영할게요를_선택하면_폐기되고_null을_반환한다() {
        saveProduct("prod_a", "PD-A", "제품A", List.of());
        RecognitionResponse recognized = recognitionService.recognize(sessionId, fakeImage());

        ConfirmRecognitionResponse response = recognitionService.confirm(
                sessionId, recognized.recognitionId(), new ConfirmRecognitionRequest("prod_a", false));

        assertThat(response).isNull();
    }

    @Test
    void 제품코드로_조회하면_제품을_찾는다() {
        saveProduct("prod_a", "PD-0091", "미니멀 크루넥 니트", List.of());

        ProductLookupResponse response = recognitionService.lookupProduct(
                sessionId, new ProductLookupRequest("PD-0091"));

        assertThat(response.productId()).isEqualTo("prod_a");
        assertThat(response.name()).isEqualTo("미니멀 크루넥 니트");
    }

    @Test
    void 존재하지_않는_제품코드면_예외를_던진다() {
        assertThatThrownBy(() -> recognitionService.lookupProduct(
                sessionId, new ProductLookupRequest("NOT-EXIST")))
                .isInstanceOf(ApiException.class);
    }
}

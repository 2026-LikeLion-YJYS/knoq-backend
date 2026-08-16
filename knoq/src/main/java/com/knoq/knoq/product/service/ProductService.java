package com.knoq.knoq.product.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.product.dto.ProductDetailResponse;
import com.knoq.knoq.product.entity.Product;
import com.knoq.knoq.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final String NO_INFO = "정보 없음";

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));

        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                orNoInfo(product.getMaterial()),
                orNoInfo(product.getFeatures()),
                product.getPrice(),
                product.getSizes(),
                product.getColors(),
                new ProductDetailResponse.Descriptions(
                        orNoInfo(product.getBrandOfficialDescription()),
                        product.getAiGeneratedDescription() // 없으면 그냥 null (명세서 예시와 동일)
                )
        );
    }

    // FR-200 준비 작업: 데모 제품 기준 사진을 등록 (원본을 base64로 저장 — 인식 요청마다 GPT 비전에 그대로 보냄)
    @Transactional
    public void registerReferenceImage(String productId, MultipartFile image) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));

        try {
            String base64 = Base64.getEncoder().encodeToString(image.getBytes());
            product.updateReferenceImage(base64);
            // @Transactional 안에서 product는 영속 상태라 dirty checking으로 자동 반영됨
        } catch (IOException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private String orNoInfo(String value) {
        return (value == null || value.isBlank()) ? NO_INFO : value;
    }
}
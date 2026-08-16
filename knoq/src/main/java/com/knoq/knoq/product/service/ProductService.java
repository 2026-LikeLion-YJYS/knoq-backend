package com.knoq.knoq.product.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.product.dto.ProductDetailResponse;
import com.knoq.knoq.product.entity.Product;
import com.knoq.knoq.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private String orNoInfo(String value) {
        return (value == null || value.isBlank()) ? NO_INFO : value;
    }
}
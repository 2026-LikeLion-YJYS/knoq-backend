package com.knoq.knoq.saved.dto.response;

import com.knoq.knoq.saved.entity.SavedProduct;
import com.knoq.knoq.saved.entity.SavedProductSource;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SavedProductResponse {

    private String savedProductId;

    private String productId;

    private SavedProductSource source;

    private LocalDateTime savedAt;

    public static SavedProductResponse from(SavedProduct savedProduct) {

        return SavedProductResponse.builder()
                .savedProductId("sav_" + savedProduct.getId())
                .productId(savedProduct.getProductId())
                .source(savedProduct.getSource())
                .savedAt(savedProduct.getSavedAt())
                .build();
    }
}
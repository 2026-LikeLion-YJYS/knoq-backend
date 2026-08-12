package com.knoq.knoq.saved.dto.response;

import com.knoq.knoq.saved.entity.SavedProduct;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SavedProductListResponse {

    private int count;

    private List<SavedProductResponse> products;

    public static SavedProductListResponse from(List<SavedProduct> savedProducts) {

        List<SavedProductResponse> responses = savedProducts.stream()
                .map(SavedProductResponse::from)
                .toList();

        return SavedProductListResponse.builder()
                .count(responses.size())
                .products(responses)
                .build();
    }
}
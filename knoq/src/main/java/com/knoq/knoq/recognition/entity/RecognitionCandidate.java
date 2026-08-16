package com.knoq.knoq.recognition.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Recognition 한 건에 딸린 후보 하나(제품 ID + 그 시점의 confidence 스냅샷)
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecognitionCandidate {

    @Column(name = "product_id", length = 64)
    private String productId;

    @Column(name = "confidence")
    private double confidence;

    private RecognitionCandidate(String productId, double confidence) {
        this.productId = productId;
        this.confidence = confidence;
    }

    public static RecognitionCandidate of(String productId, double confidence) {
        return new RecognitionCandidate(productId, confidence);
    }
}
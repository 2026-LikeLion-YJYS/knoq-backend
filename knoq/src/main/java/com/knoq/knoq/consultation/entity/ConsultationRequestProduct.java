package com.knoq.knoq.consultation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "consultation_request_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsultationRequestProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consultation_request_id", nullable = false)
    private ConsultationRequest consultationRequest;

    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    private ConsultationRequestProduct(ConsultationRequest consultationRequest, String productId) {
        this.consultationRequest = consultationRequest;
        this.productId = productId;
    }

    static ConsultationRequestProduct of(ConsultationRequest consultationRequest, String productId) {
        return new ConsultationRequestProduct(consultationRequest, productId);
    }
}

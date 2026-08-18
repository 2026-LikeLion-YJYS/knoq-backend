package com.knoq.knoq.consultation.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "consultation_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsultationRequest {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "help_type", nullable = false, length = 40)
    private HelpType helpType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;

    @Column(name = "include_needs_analysis", nullable = false)
    private boolean includeNeedsAnalysis;

    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "consultationRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConsultationRequestProduct> products = new ArrayList<>();

    private ConsultationRequest(String id, String sessionId, Long storeId, HelpType helpType,
                                boolean includeNeedsAnalysis) {
        this.id = id;
        this.sessionId = sessionId;
        this.storeId = storeId;
        this.helpType = helpType;
        this.status = RequestStatus.REQUESTED;
        this.includeNeedsAnalysis = includeNeedsAnalysis;
        this.updatedAt = LocalDateTime.now();
    }

    public static ConsultationRequest of(String id, String sessionId, Long storeId, HelpType helpType,
                                         boolean includeNeedsAnalysis) {
        return new ConsultationRequest(id, sessionId, storeId, helpType, includeNeedsAnalysis);
    }

    public void addProduct(String productId) {
        products.add(ConsultationRequestProduct.of(this, productId));
    }

    public void updateStatus(RequestStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}

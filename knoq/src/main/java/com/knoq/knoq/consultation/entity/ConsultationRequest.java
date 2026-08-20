package com.knoq.knoq.consultation.entity;

import com.knoq.knoq.needs.entity.NeedsAnalysis;
import com.knoq.knoq.sessions.entity.LifestyleTag;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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

    @Column(name = "nickname_snapshot", length = 10)
    private String nicknameSnapshot;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "consultation_request_lifestyle_tag",
            joinColumns = @JoinColumn(name = "consultation_request_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "tag", nullable = false, length = 20)
    private List<LifestyleTag> lifestyleTagsSnapshot = new ArrayList<>();

    @Column(name = "needs_product_category_snapshot", length = 100)
    private String needsProductCategorySnapshot;

    @Column(name = "needs_preferred_color_snapshot", length = 100)
    private String needsPreferredColorSnapshot;

    @Column(name = "needs_preferred_material_snapshot", length = 100)
    private String needsPreferredMaterialSnapshot;

    @Column(name = "needs_preferred_size_snapshot", length = 50)
    private String needsPreferredSizeSnapshot;

    @Column(name = "needs_comment_snapshot", length = 500)
    private String needsCommentSnapshot;

    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "consultationRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConsultationRequestProduct> products = new ArrayList<>();

    private ConsultationRequest(String id, String sessionId, Long storeId, HelpType helpType,
                                boolean includeNeedsAnalysis, String nicknameSnapshot,
                                List<LifestyleTag> lifestyleTagsSnapshot) {
        this.id = id;
        this.sessionId = sessionId;
        this.storeId = storeId;
        this.helpType = helpType;
        this.status = RequestStatus.REQUESTED;
        this.includeNeedsAnalysis = includeNeedsAnalysis;
        this.nicknameSnapshot = nicknameSnapshot;
        this.lifestyleTagsSnapshot = lifestyleTagsSnapshot == null
                ? new ArrayList<>()
                : new ArrayList<>(lifestyleTagsSnapshot);
        this.updatedAt = LocalDateTime.now();
    }

    public static ConsultationRequest of(String id, String sessionId, Long storeId, HelpType helpType,
                                         boolean includeNeedsAnalysis) {
        return new ConsultationRequest(
                id, sessionId, storeId, helpType, includeNeedsAnalysis, null, List.of()
        );
    }

    public static ConsultationRequest of(String id, String sessionId, Long storeId, HelpType helpType,
                                         boolean includeNeedsAnalysis, String nicknameSnapshot,
                                         List<LifestyleTag> lifestyleTagsSnapshot) {
        return new ConsultationRequest(
                id, sessionId, storeId, helpType, includeNeedsAnalysis,
                nicknameSnapshot, lifestyleTagsSnapshot
        );
    }

    public void snapshotNeedsAnalysis(NeedsAnalysis needsAnalysis) {
        this.needsProductCategorySnapshot = needsAnalysis.getProductCategory();
        this.needsPreferredColorSnapshot = needsAnalysis.getPreferredColor();
        this.needsPreferredMaterialSnapshot = needsAnalysis.getPreferredMaterial();
        this.needsPreferredSizeSnapshot = needsAnalysis.getPreferredSize();
        this.needsCommentSnapshot = needsAnalysis.getComment();
    }

    public boolean hasNeedsAnalysisSnapshot() {
        return needsProductCategorySnapshot != null
                || needsPreferredColorSnapshot != null
                || needsPreferredMaterialSnapshot != null
                || needsPreferredSizeSnapshot != null
                || needsCommentSnapshot != null;
    }

    public void addProduct(String productId) {
        products.add(ConsultationRequestProduct.of(this, productId));
    }

    public void updateStatus(RequestStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}

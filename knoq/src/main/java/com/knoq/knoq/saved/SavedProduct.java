package com.knoq.knoq.saved;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "saved_product",
        uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "product_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SavedProductSource source;

    @Column(name = "saved_at", nullable = false)
    private LocalDateTime savedAt;

    private SavedProduct(String sessionId, String productId, SavedProductSource source) {
        this.sessionId = sessionId;
        this.productId = productId;
        this.source = source;
        this.savedAt = LocalDateTime.now();
    }

    public static SavedProduct ofCamera(String sessionId, String productId) {
        return new SavedProduct(sessionId, productId, SavedProductSource.CAMERA);
    }

    public static SavedProduct ofRecommend(String sessionId, String productId) {
        return new SavedProduct(sessionId, productId, SavedProductSource.RECOMMEND);
    }
}
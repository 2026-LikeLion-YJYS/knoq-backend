package com.knoq.knoq.needs.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "needs_analysis", uniqueConstraints = @UniqueConstraint(columnNames = "session_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NeedsAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true, length = 64)
    private String sessionId;

    @Column(name = "product_category", length = 100)
    private String productCategory;

    @Column(name = "preferred_color", length = 100)
    private String preferredColor;

    @Column(name = "preferred_material", length = 100)
    private String preferredMaterial;

    @Column(name = "preferred_size", length = 50)
    private String preferredSize;

    @Column(length = 500)
    private String comment;

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;

    private NeedsAnalysis(String sessionId) {
        this.sessionId = sessionId;
    }

    public static NeedsAnalysis of(String sessionId) {
        return new NeedsAnalysis(sessionId);
    }

    public void updateResult(String productCategory, String preferredColor,
                             String preferredMaterial, String preferredSize, String comment) {
        this.productCategory = productCategory;
        this.preferredColor = preferredColor;
        this.preferredMaterial = preferredMaterial;
        this.preferredSize = preferredSize;
        this.comment = comment;
        this.analyzedAt = LocalDateTime.now();
    }
}
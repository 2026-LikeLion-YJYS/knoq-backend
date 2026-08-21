package com.knoq.knoq.recognition.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

// 카메라 인식 요청 1건. "찾아낸 후보들"을 confirm 전까지 임시로 들고 있는 역할
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recognition {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false, length = 20)
    private MatchType matchType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecognitionStatus status = RecognitionStatus.PENDING;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recognition_candidate", joinColumns = @JoinColumn(name = "recognition_id"))
    private List<RecognitionCandidate> candidates;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Recognition(String id, String sessionId, MatchType matchType, List<RecognitionCandidate> candidates) {
        this.id = id;
        this.sessionId = sessionId;
        this.matchType = matchType;
        this.candidates = candidates;
    }

    public static Recognition of(String id, String sessionId, MatchType matchType, List<RecognitionCandidate> candidates) {
        return new Recognition(id, sessionId, matchType, candidates);
    }

    public boolean hasCandidate(String productId) {
        return candidates.stream().anyMatch(c -> c.getProductId().equals(productId));
    }

    public void confirm() {
        this.status = RecognitionStatus.CONFIRMED;
    }

    public void discard() {
        this.status = RecognitionStatus.DISCARDED;
    }
}
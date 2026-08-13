package com.knoq.knoq.sessions.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Session {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // FR-002: 약관 동의. 기본값은 전부 false (아직 동의 안 함)
    @Column(name = "terms_of_service", nullable = false)
    private boolean termsOfService = false;

    @Column(name = "privacy_policy", nullable = false)
    private boolean privacyPolicy = false;

    @Column(name = "over14", nullable = false)
    private boolean over14 = false;

    @Column(name = "marketing_opt_in", nullable = false)
    private boolean marketingOptIn = false;

    @Column(name = "consented_at")
    private LocalDateTime consentedAt;

    private Session(String id, String token, Long storeId, LocalDateTime expiresAt) {
        this.id = id;
        this.token = token;
        this.storeId = storeId;
        this.expiresAt = expiresAt;
    }

    public static Session of(String id, String token, Long storeId, LocalDateTime expiresAt) {
        return new Session(id, token, storeId, expiresAt);
    }

    // Setter 대신 "약관에 동의한다"는 의도가 명확한 전용 메서드로 값 변경
    public void agreeConsents(boolean termsOfService, boolean privacyPolicy, boolean over14,
                              boolean marketingOptIn, LocalDateTime consentedAt) {
        this.termsOfService = termsOfService;
        this.privacyPolicy = privacyPolicy;
        this.over14 = over14;
        this.marketingOptIn = marketingOptIn;
        this.consentedAt = consentedAt;
    }
}
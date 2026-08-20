package com.knoq.knoq.sessions.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // FR-100: 저장 범위. 기본값 PRIVATE(계정 연결 안 함)
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_scope", nullable = false, length = 20)
    private StorageScope storageScope = StorageScope.PRIVATE;

    @Column(name = "account_id", length = 64)
    private String accountId;

    // FR-101: 닉네임. 처음엔 없다가(null) 설정되면 채워짐
    @Column(length = 10)
    private String nickname;

    // FR-102: 라이프스타일 태그. 값이 여러 개라 별도 테이블(session_lifestyle_tag)에 저장됨
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "session_lifestyle_tag", joinColumns = @JoinColumn(name = "session_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tag")
    private List<LifestyleTag> lifestyleTags = new ArrayList<>();

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

    // "ACCOUNT로 저장할래" 선택 = 아직 카카오 로그인은 안 했으니 중간 상태로
    public void requestAccountStorage() {
        this.storageScope = StorageScope.PENDING_KAKAO_LOGIN;
    }

    // "PRIVATE로 저장할래" 선택 (또는 카카오 로그인 실패 시 되돌아가는 상태)
    public void usePrivateStorage() {
        this.storageScope = StorageScope.PRIVATE;
        this.accountId = null;
    }

    // 카카오 로그인 성공 → 실제 계정 연결 완료
    public void linkAccount(String accountId) {
        this.storageScope = StorageScope.ACCOUNT;
        this.accountId = accountId;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateLifestyleTags(List<LifestyleTag> tags) {
        this.lifestyleTags.clear();
        this.lifestyleTags.addAll(tags);
    }

    public void refreshExpiration(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void updateStore(Long storeId) {
        this.storeId = storeId;
    }

    // FR-400: ACCOUNT 세션 종료 — 지우지 않고 즉시 만료 처리만 함 (계정 데이터는 남아야 하니까)
    public void endSession() {
        this.expiresAt = LocalDateTime.now();
    }
}

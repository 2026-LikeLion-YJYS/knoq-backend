package com.knoq.knoq.sessions.service;

import com.knoq.knoq.account.entity.Account;
import com.knoq.knoq.account.repository.AccountRepository;
import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.sessions.client.KakaoApiClient;
import com.knoq.knoq.sessions.dto.ConsentRequest;
import com.knoq.knoq.sessions.dto.ConsentType;
import com.knoq.knoq.sessions.dto.ConsentsResponse;
import com.knoq.knoq.sessions.dto.CreateSessionRequest;
import com.knoq.knoq.sessions.dto.CreateSessionResponse;
import com.knoq.knoq.sessions.dto.GetSessionResponse;
import com.knoq.knoq.sessions.dto.KakaoLoginRequest;
import com.knoq.knoq.sessions.dto.KakaoLoginResponse;
import com.knoq.knoq.sessions.dto.StorageScopeRequest;
import com.knoq.knoq.sessions.dto.StorageScopeResponse;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.entity.StorageScope;
import com.knoq.knoq.sessions.repository.SessionRepository;
import com.knoq.knoq.store.entity.Store;
import com.knoq.knoq.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final String ID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SessionRepository sessionRepository;
    private final StoreRepository storeRepository;
    private final AccountRepository accountRepository;
    private final KakaoApiClient kakaoApiClient;

    // application.yml에 knoq.session.expiry-minutes가 없으면 기본값 60을 씀
    @Value("${knoq.session.expiry-minutes:60}")
    private long expiryMinutes;

    @Transactional
    public CreateSessionResponse createSession(CreateSessionRequest request) {
        // 1. storeCode로 매장 찾기. 없으면 여기서 바로 에러를 던지고 끝냄 (세션은 만들지 않음)
        Store store = storeRepository.findByStoreCode(request.storeCode())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_STORE_CODE));

        // 2. 세션 만들기
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes);
        Session session = Session.of(
                generateSessionId(),
                generateToken(),
                store.getId(),
                expiresAt
        );

        // 3. DB에 저장
        sessionRepository.save(session);

        // 4. 응답 형태로 바꿔서 반환 (storeName은 Store에서 가져옴)
        return new CreateSessionResponse(
                session.getId(),
                session.getToken(),
                store.getStoreName(),
                session.getExpiresAt()
        );
    }

    @Transactional(readOnly = true)
    public GetSessionResponse getSession(String sessionId) {
        Session session = findValidSession(sessionId);

        // storeId로 매장 이름 찾기
        Store store = storeRepository.findById(session.getStoreId())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_STORE_CODE));

        return new GetSessionResponse(
                session.getId(),
                store.getStoreName(),
                session.getExpiresAt()
        );
    }

    @Transactional
    public ConsentsResponse agreeConsents(String sessionId, ConsentRequest request) {
        Session session = findValidSession(sessionId);

        // 필수 3개(이용약관·개인정보·만14세) 중 하나라도 false면 400
        if (!request.termsOfService() || !request.privacyPolicy() || !request.over14()) {
            throw new ApiException(ErrorCode.CONSENT_REQUIRED);
        }

        LocalDateTime consentedAt = LocalDateTime.now();
        session.agreeConsents(
                request.termsOfService(),
                request.privacyPolicy(),
                request.over14(),
                request.marketingOptIn(),
                consentedAt
        );
        // sessionRepository.save() 호출 안 함: session은 findValidSession에서 findById로 가져온
        // "영속 상태" 객체라서, @Transactional 메서드가 끝날 때 바뀐 값이 자동으로 DB에 반영됨 (더티 체킹)

        return new ConsentsResponse(List.of(
                new ConsentsResponse.ConsentItem(ConsentType.TERMS_OF_SERVICE, request.termsOfService(), consentedAt),
                new ConsentsResponse.ConsentItem(ConsentType.PRIVACY_POLICY, request.privacyPolicy(), consentedAt),
                new ConsentsResponse.ConsentItem(ConsentType.OVER14, request.over14(), consentedAt),
                new ConsentsResponse.ConsentItem(ConsentType.MARKETING_OPT_IN, request.marketingOptIn(), consentedAt)
        ));
    }

    @Transactional
    public StorageScopeResponse selectStorageScope(String sessionId, StorageScopeRequest request) {
        Session session = findValidSession(sessionId);

        StorageScope requested = request.storageScope();
        // 클라이언트가 PENDING_KAKAO_LOGIN을 직접 보내는 건 허용 안 함 (시스템이 계산하는 상태값)
        if (requested == StorageScope.PENDING_KAKAO_LOGIN) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        if (requested == StorageScope.ACCOUNT) {
            // 아직 카카오 로그인 전이니 바로 ACCOUNT로 못 가고, "로그인 대기" 상태로만 바꿈
            session.requestAccountStorage();
            return new StorageScopeResponse(session.getStorageScope(), true);
        }

        session.usePrivateStorage();
        return new StorageScopeResponse(session.getStorageScope(), false);
    }

    @Transactional
    public KakaoLoginResponse kakaoLogin(String sessionId, KakaoLoginRequest request) {
        Session session = findValidSession(sessionId);

        Optional<Long> kakaoUserId = kakaoApiClient.getKakaoUserId(request.kakaoAccessToken());

        if (kakaoUserId.isEmpty()) {
            // 카카오 인증 실패해도 에러로 막지 않고, PRIVATE로 되돌린 뒤 200으로 응답
            session.usePrivateStorage();
            return new KakaoLoginResponse(StorageScope.PRIVATE, null);
        }

        // 이 카카오 계정으로 이미 만들어진 Account가 있으면 재사용, 없으면 새로 생성
        Account account = accountRepository.findByKakaoUserId(kakaoUserId.get())
                .orElseGet(() -> accountRepository.save(Account.of(generateAccountId(), kakaoUserId.get())));

        session.linkAccount(account.getId());

        return new KakaoLoginResponse(session.getStorageScope(), account.getId());
    }

    // sessionId로 세션을 찾고, 없으면 404 / 만료됐으면 410을 던지는 공통 로직
    // (getSession, agreeConsents 둘 다 "유효한 세션인지 확인"이 먼저 필요해서 메서드로 뺌)
    private Session findValidSession(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.SESSION_EXPIRED);
        }

        return session;
    }

    private String generateSessionId() {
        StringBuilder sb = new StringBuilder("sess_");
        for (int i = 0; i < 12; i++) {
            sb.append(ID_CHARS.charAt(RANDOM.nextInt(ID_CHARS.length())));
        }
        return sb.toString();
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateAccountId() {
        StringBuilder sb = new StringBuilder("acct_");
        for (int i = 0; i < 12; i++) {
            sb.append(ID_CHARS.charAt(RANDOM.nextInt(ID_CHARS.length())));
        }
        return sb.toString();
    }
}
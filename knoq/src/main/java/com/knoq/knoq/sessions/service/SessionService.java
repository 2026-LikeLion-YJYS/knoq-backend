package com.knoq.knoq.sessions.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.sessions.dto.CreateSessionRequest;
import com.knoq.knoq.sessions.dto.CreateSessionResponse;
import com.knoq.knoq.sessions.dto.GetSessionResponse;
import com.knoq.knoq.sessions.entity.Session;
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

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final String ID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SessionRepository sessionRepository;
    private final StoreRepository storeRepository;

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
        // 1. sessionId로 세션 찾기. 없으면 404
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(ErrorCode.SESSION_NOT_FOUND));

        // 2. 만료 시각이 지났으면 410 (조회 자체는 됐지만 이미 유효기간이 끝났다는 뜻)
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.SESSION_EXPIRED);
        }

        // 3. storeId로 매장 이름 찾기
        Store store = storeRepository.findById(session.getStoreId())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_STORE_CODE));

        // 4. 응답 형태로 반환
        return new GetSessionResponse(
                session.getId(),
                store.getStoreName(),
                session.getExpiresAt()
        );
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
}
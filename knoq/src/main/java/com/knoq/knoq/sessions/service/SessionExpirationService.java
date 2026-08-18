package com.knoq.knoq.sessions.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessionExpirationService {

    private final SessionRepository sessionRepository;

    @Value("${knoq.session.expiry-minutes:60}")
    private long expiryMinutes;

    @Transactional(readOnly = true)
    public Session getValidSession(String sessionId) {
        return findValidSession(sessionId);
    }

    @Transactional
    public Session getValidSessionAndRefresh(String sessionId) {
        Session session = findValidSession(sessionId);
        session.refreshExpiration(LocalDateTime.now().plusMinutes(expiryMinutes));
        return session;
    }

    private Session findValidSession(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.SESSION_EXPIRED);
        }

        return session;
    }
}

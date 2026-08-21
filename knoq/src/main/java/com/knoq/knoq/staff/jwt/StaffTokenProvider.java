package com.knoq.knoq.staff.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * staffToken(JWT) 발급/검증 전용 클래스.
 * B팀 요청대로, 토큰 안에 storeCode 클레임을 넣어서 B가 경로 파라미터 없이
 * 토큰만 보고 어느 매장인지 알 수 있게 함.
 */
@Component
public class StaffTokenProvider {

    @Value("${knoq.jwt.secret}")
    private String secret;

    @Value("${knoq.staff.token-expiry-hours:12}")
    private long expiryHours;

    public String generateToken(String staffSessionId, String storeCode) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date expiration = Date.from(Instant.now().plus(expiryHours, ChronoUnit.HOURS));

        return Jwts.builder()
                .subject(staffSessionId)
                .claim("storeCode", storeCode)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    // 서명이 위조됐거나 만료됐으면 여기서 예외가 터짐 (호출한 쪽에서 잡아서 401 처리)
    public Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
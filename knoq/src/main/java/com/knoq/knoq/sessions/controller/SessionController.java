package com.knoq.knoq.sessions.controller;

import com.knoq.knoq.sessions.dto.ConsentRequest;
import com.knoq.knoq.sessions.dto.ConsentsResponse;
import com.knoq.knoq.sessions.dto.CreateSessionRequest;
import com.knoq.knoq.sessions.dto.CreateSessionResponse;
import com.knoq.knoq.sessions.dto.GetSessionResponse;
import com.knoq.knoq.sessions.dto.KakaoLoginRequest;
import com.knoq.knoq.sessions.dto.KakaoLoginResponse;
import com.knoq.knoq.sessions.dto.StorageScopeRequest;
import com.knoq.knoq.sessions.dto.StorageScopeResponse;
import com.knoq.knoq.sessions.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Session", description = "매장 진입 및 세션 API")
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @Operation(summary = "매장 진입 및 세션 생성")
    @PostMapping
    public ResponseEntity<CreateSessionResponse> createSession(
            @Valid @RequestBody CreateSessionRequest request
    ) {
        CreateSessionResponse response = sessionService.createSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "세션 조회 (새로고침/재진입 시 상태 복원용)")
    @GetMapping("/{sessionId}")
    public ResponseEntity<GetSessionResponse> getSession(
            @PathVariable String sessionId
    ) {
        GetSessionResponse response = sessionService.getSession(sessionId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "약관 동의")
    @PutMapping("/{sessionId}/consents")
    public ResponseEntity<ConsentsResponse> agreeConsents(
            @PathVariable String sessionId,
            @Valid @RequestBody ConsentRequest request
    ) {
        ConsentsResponse response = sessionService.agreeConsents(sessionId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "저장 범위 선택")
    @PutMapping("/{sessionId}/storage-scope")
    public ResponseEntity<StorageScopeResponse> selectStorageScope(
            @PathVariable String sessionId,
            @Valid @RequestBody StorageScopeRequest request
    ) {
        StorageScopeResponse response = sessionService.selectStorageScope(sessionId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카카오 로그인")
    @PostMapping("/{sessionId}/kakao-login")
    public ResponseEntity<KakaoLoginResponse> kakaoLogin(
            @PathVariable String sessionId,
            @Valid @RequestBody KakaoLoginRequest request
    ) {
        KakaoLoginResponse response = sessionService.kakaoLogin(sessionId, request);
        return ResponseEntity.ok(response);
    }
}
package com.knoq.knoq.sessions.controller;

import com.knoq.knoq.sessions.dto.ConsentRequest;
import com.knoq.knoq.sessions.dto.ConsentsResponse;
import com.knoq.knoq.sessions.dto.CreateSessionRequest;
import com.knoq.knoq.sessions.dto.CreateSessionResponse;
import com.knoq.knoq.sessions.dto.GetSessionResponse;
import com.knoq.knoq.sessions.dto.KakaoLoginRequest;
import com.knoq.knoq.sessions.dto.KakaoLoginResponse;
import com.knoq.knoq.sessions.dto.LifestyleTagsRequest;
import com.knoq.knoq.sessions.dto.LifestyleTagsResponse;
import com.knoq.knoq.sessions.dto.NicknameRequest;
import com.knoq.knoq.sessions.dto.NicknameResponse;
import com.knoq.knoq.sessions.dto.SessionArchiveResponse;
import com.knoq.knoq.sessions.dto.StorageScopeRequest;
import com.knoq.knoq.sessions.dto.StorageScopeResponse;
import com.knoq.knoq.sessions.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
            summary = "탐색 아카이브 조회",
            description = "ACCOUNT 로그인 상태에서 같은 고객의 방문 기록과 저장 제품을 최근순으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탐색 아카이브 조회 성공"),
            @ApiResponse(responseCode = "401", description = "ACCOUNT 로그인이 필요함"),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
            @ApiResponse(responseCode = "410", description = "현재 세션이 만료됨")
    })
    @GetMapping("/{sessionId}/archive")
    public ResponseEntity<SessionArchiveResponse> getArchive(
            @PathVariable String sessionId
    ) {
        return ResponseEntity.ok(sessionService.getArchive(sessionId));
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

    @Operation(
            summary = "카카오 로그인",
            description = "같은 계정 + 같은 매장으로 오늘 이미 로그인한 세션이 있으면 그 세션으로 갈아탑니다. "
                    + "이 경우 응답의 sessionId/sessionToken이 요청에 쓴 것과 달라지므로, 이후 요청은 "
                    + "반드시 응답에 담긴 sessionId/sessionToken을 사용해야 합니다."
    )
    @PostMapping("/{sessionId}/kakao-login")
    public ResponseEntity<KakaoLoginResponse> kakaoLogin(
            @PathVariable String sessionId,
            @Valid @RequestBody KakaoLoginRequest request
    ) {
        KakaoLoginResponse response = sessionService.kakaoLogin(sessionId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "닉네임 설정")
    @PutMapping("/{sessionId}/nickname")
    public ResponseEntity<NicknameResponse> setNickname(
            @PathVariable String sessionId,
            @Valid @RequestBody NicknameRequest request
    ) {
        NicknameResponse response = sessionService.setNickname(sessionId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "라이프스타일 태그 설정")
    @PutMapping("/{sessionId}/lifestyle-tags")
    public ResponseEntity<LifestyleTagsResponse> updateLifestyleTags(
            @PathVariable String sessionId,
            @Valid @RequestBody LifestyleTagsRequest request
    ) {
        LifestyleTagsResponse response = sessionService.updateLifestyleTags(sessionId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "쇼핑 마치기")
    @PostMapping("/{sessionId}/finish")
    public ResponseEntity<Void> finishShopping(
            @PathVariable String sessionId
    ) {
        sessionService.finishShopping(sessionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/{sessionId}/logout")
    public ResponseEntity<Void> logout(
            @PathVariable String sessionId
    ) {
        sessionService.logout(sessionId);
        return ResponseEntity.noContent().build();
    }
}

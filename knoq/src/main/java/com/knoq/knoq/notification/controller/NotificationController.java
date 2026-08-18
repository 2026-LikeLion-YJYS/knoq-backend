package com.knoq.knoq.notification.controller;

import com.knoq.knoq.notification.dto.response.NotificationListResponse;
import com.knoq.knoq.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions/{sessionId}/notifications")
@Tag(name = "Notification", description = "고객 상담 알림 API")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(
            summary = "알림 목록 조회",
            description = "고객의 상담 알림을 최신순으로 조회합니다. 폴링 조회이므로 세션 만료시각은 갱신하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationListResponse.class))),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
            @ApiResponse(responseCode = "410", description = "세션 만료")
    })
    public ResponseEntity<NotificationListResponse> findAll(
            @Parameter(example = "sess_abc123") @PathVariable String sessionId
    ) {
        return ResponseEntity.ok(notificationService.findAll(sessionId));
    }
}

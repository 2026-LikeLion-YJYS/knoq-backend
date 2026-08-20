package com.knoq.knoq.sessions.dto;

import com.knoq.knoq.saved.entity.SavedProductSource;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "로그인한 고객의 탐색 아카이브")
public record SessionArchiveResponse(
        @Schema(description = "전체 방문 횟수", example = "2")
        int count,

        @Schema(description = "최근 방문부터 정렬된 목록")
        List<Visit> visits
) {
    public record Visit(
            @Schema(example = "sess_abc123def456")
            String sessionId,

            @Schema(description = "방문(세션 생성) 시각", example = "2025-08-16T14:30:00")
            LocalDateTime visitedAt,

            @Schema(description = "현재 접속 중인 세션인지 여부", example = "true")
            boolean isCurrent,

            @Schema(description = "해당 방문에서 저장한 제품들")
            List<ArchivedProduct> products
    ) {
    }

    public record ArchivedProduct(
            @Schema(example = "prod_1")
            String productId,

            @Schema(example = "Tracy 비세토스 호보")
            String name,

            @Schema(example = "/demo/products/prod_1/front.png")
            String thumbnailUrl,

            SavedProductSource source,

            LocalDateTime savedAt
    ) {
    }
}

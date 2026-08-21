package com.knoq.knoq.consultation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.knoq.knoq.consultation.entity.HelpType;
import com.knoq.knoq.consultation.entity.RequestStatus;
import com.knoq.knoq.needs.dto.response.NeedsAnalysisSummary;
import com.knoq.knoq.product.dto.ProductDetailResponse;
import com.knoq.knoq.sessions.entity.LifestyleTag;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StaffRequestDetailResponse(
        @Schema(example = "req_27")
        String requestId,

        @Schema(example = "노크고객")
        String nickname,

        @Schema(example = "PRODUCT_COMPARISON")
        HelpType helpType,

        List<LifestyleTag> lifestyleTags,

        List<ProductDetailResponse> products,

        NeedsAnalysisSummary needsAnalysis,

        @Schema(example = "REQUESTED")
        RequestStatus status
) {
}

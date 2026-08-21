package com.knoq.knoq.consultation.dto.response;

import java.util.List;

public record StaffRequestInboxResponse(
        List<StaffRequestSummaryResponse> requests
) {
}

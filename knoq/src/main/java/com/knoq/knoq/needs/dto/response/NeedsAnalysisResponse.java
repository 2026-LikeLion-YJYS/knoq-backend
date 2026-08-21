package com.knoq.knoq.needs.dto.response;

import com.knoq.knoq.needs.entity.NeedsAnalysis;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NeedsAnalysisResponse {

    private boolean canAnalyze;
    private long savedCount;
    private NeedsAnalysisSummary analysis;

    public static NeedsAnalysisResponse of(boolean canAnalyze, long savedCount, NeedsAnalysis needsAnalysis) {
        return NeedsAnalysisResponse.builder()
                .canAnalyze(canAnalyze)
                .savedCount(savedCount)
                .analysis(needsAnalysis == null ? null : NeedsAnalysisSummary.from(needsAnalysis))
                .build();
    }
}
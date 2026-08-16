package com.knoq.knoq.recognition.dto;

import com.knoq.knoq.recognition.entity.MatchType;

import java.util.List;

public record RecognitionResponse(
        String recognitionId,
        MatchType matchType,
        List<CandidateResponse> candidates
) {
    public record CandidateResponse(
            String productId,
            String name,
            String thumbnailUrl,
            double confidence
    ) {}
}
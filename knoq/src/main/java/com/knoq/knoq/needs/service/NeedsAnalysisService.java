package com.knoq.knoq.needs.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.needs.dto.response.NeedsAnalysisResponse;
import com.knoq.knoq.needs.dto.response.NeedsAnalysisResultResponse;
import com.knoq.knoq.needs.entity.NeedsAnalysis;
import com.knoq.knoq.needs.repository.NeedsAnalysisRepository;
import com.knoq.knoq.saved.entity.SavedProduct;
import com.knoq.knoq.saved.repository.SavedProductRepository;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NeedsAnalysisService {

    private static final int MIN_SAVED_PRODUCTS_TO_ANALYZE = 2;

    private final NeedsAnalysisRepository needsAnalysisRepository;
    private final SavedProductRepository savedProductRepository;
    private final SessionRepository sessionRepository;
    private final ProductAttributeProvider productAttributeProvider;

    @Transactional(readOnly = true)
    public NeedsAnalysisResponse getAnalysis(String sessionId) {
        validateSession(sessionId);

        long savedCount = savedProductRepository.countBySessionId(sessionId);
        NeedsAnalysis analysis = needsAnalysisRepository.findBySessionId(sessionId).orElse(null);

        return NeedsAnalysisResponse.of(
                savedCount >= MIN_SAVED_PRODUCTS_TO_ANALYZE,
                savedCount,
                analysis
        );
    }

    @Transactional
    public NeedsAnalysisResultResponse analyze(String sessionId) {
        validateSession(sessionId);

        List<SavedProduct> savedProducts = savedProductRepository.findBySessionIdOrderBySavedAtDesc(sessionId);
        if (savedProducts.size() < MIN_SAVED_PRODUCTS_TO_ANALYZE) {
            throw new ApiException(ErrorCode.NEEDS_ANALYSIS_NOT_ENOUGH_SAVED_PRODUCTS);
        }

        List<String> productIds = savedProducts.stream().map(SavedProduct::getProductId).toList();
        List<ProductAttributes> attributes = productAttributeProvider.getAttributes(productIds);

        String category = NeedsAnalysisAggregator.mostFrequent(
                attributes.stream().map(ProductAttributes::category).toList());
        String material = NeedsAnalysisAggregator.mostFrequent(
                attributes.stream().map(ProductAttributes::material).toList());
        String color = NeedsAnalysisAggregator.mostFrequent(
                attributes.stream().flatMap(a -> a.colors().stream()).toList());
        String size = NeedsAnalysisAggregator.mostFrequent(
                attributes.stream().flatMap(a -> a.sizes().stream()).toList());
        String comment = NeedsAnalysisAggregator.buildComment(color, material, size);

        NeedsAnalysis needsAnalysis = needsAnalysisRepository.findBySessionId(sessionId)
                .orElseGet(() -> NeedsAnalysis.of(sessionId));
        needsAnalysis.updateResult(category, color, material, size, comment);
        needsAnalysisRepository.save(needsAnalysis);

        return NeedsAnalysisResultResponse.from(needsAnalysis);
    }

    private void validateSession(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(ErrorCode.SESSION_NOT_FOUND));
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.SESSION_EXPIRED);
        }
    }
}
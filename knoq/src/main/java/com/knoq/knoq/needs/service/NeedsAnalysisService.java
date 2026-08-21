package com.knoq.knoq.needs.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.needs.dto.request.UpdateNeedsAnalysisRequest;
import com.knoq.knoq.needs.dto.response.NeedsAnalysisResponse;
import com.knoq.knoq.needs.dto.response.NeedsAnalysisResultResponse;
import com.knoq.knoq.needs.entity.NeedsAnalysis;
import com.knoq.knoq.needs.repository.NeedsAnalysisRepository;
import com.knoq.knoq.needs.support.PreferredMaterialNormalizer;
import com.knoq.knoq.saved.entity.SavedProduct;
import com.knoq.knoq.saved.repository.SavedProductRepository;
import com.knoq.knoq.sessions.service.SessionExpirationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NeedsAnalysisService {

    private static final int MIN_SAVED_PRODUCTS_TO_ANALYZE = 2;

    private final NeedsAnalysisRepository needsAnalysisRepository;
    private final SavedProductRepository savedProductRepository;
    private final SessionExpirationService sessionExpirationService;
    private final ProductAttributeProvider productAttributeProvider;
    private final NeedsCommentGenerator needsCommentGenerator;

    @Transactional
    public NeedsAnalysisResponse getAnalysis(String sessionId) {
        sessionExpirationService.getValidSessionAndRefresh(sessionId);

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
        sessionExpirationService.getValidSessionAndRefresh(sessionId);

        List<SavedProduct> savedProducts = savedProductRepository.findBySessionIdOrderBySavedAtDesc(sessionId);
        if (savedProducts.size() < MIN_SAVED_PRODUCTS_TO_ANALYZE) {
            throw new ApiException(ErrorCode.NEEDS_ANALYSIS_NOT_ENOUGH_SAVED_PRODUCTS);
        }

        NeedsAnalysis needsAnalysis = needsAnalysisRepository.findBySessionId(sessionId)
                .orElseGet(() -> NeedsAnalysis.of(sessionId));

        // 사용자가 PUT으로 네 항목을 이미 직접 수정했다면, 재분석해도 그 값은 그대로 두고 comment만 새로 만듦
        if (needsAnalysis.isUserEdited()) {
            String generatedComment = needsCommentGenerator.generateFromSelections(
                    needsAnalysis.getProductCategory(),
                    needsAnalysis.getPreferredColor(),
                    needsAnalysis.getPreferredMaterial(),
                    needsAnalysis.getPreferredSize()
            );
            String comment = (generatedComment == null || generatedComment.isBlank())
                    ? NeedsAnalysisAggregator.buildSelectionComment(
                            needsAnalysis.getProductCategory(),
                            needsAnalysis.getPreferredColor(),
                            needsAnalysis.getPreferredMaterial(),
                            needsAnalysis.getPreferredSize()
                    )
                    : generatedComment;
            needsAnalysis.updateComment(comment);
            needsAnalysisRepository.save(needsAnalysis);
            return NeedsAnalysisResultResponse.from(needsAnalysis);
        }

        List<String> productIds = savedProducts.stream().map(SavedProduct::getProductId).toList();
        List<ProductAttributes> attributes = productAttributeProvider.getAttributes(productIds);

        String category = NeedsAnalysisAggregator.mostFrequent(
                attributes.stream().map(ProductAttributes::category).toList());
        String material = NeedsAnalysisAggregator.mostFrequent(
                attributes.stream()
                        .map(ProductAttributes::material)
                        .map(PreferredMaterialNormalizer::normalize)
                        .toList());
        String color = NeedsAnalysisAggregator.mostFrequent(
                attributes.stream().flatMap(a -> a.colors().stream()).toList());
        String size = NeedsAnalysisAggregator.mostFrequent(
                attributes.stream().flatMap(a -> a.sizes().stream()).toList());
        String templateComment = NeedsAnalysisAggregator.buildComment(color, material, size);
        String generatedComment = needsCommentGenerator.generate(attributes);
        String comment = (generatedComment == null || generatedComment.isBlank())
                ? templateComment
                : generatedComment;

        needsAnalysis.updateResult(category, color, material, size, comment);
        needsAnalysisRepository.save(needsAnalysis);

        return NeedsAnalysisResultResponse.from(needsAnalysis);
    }

    @Transactional
    public NeedsAnalysisResultResponse updateAnalysis(
            String sessionId,
            UpdateNeedsAnalysisRequest request
    ) {
        sessionExpirationService.getValidSessionAndRefresh(sessionId);

        NeedsAnalysis needsAnalysis = needsAnalysisRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

        needsAnalysis.updateUserSelections(
                request.productCategory(),
                request.preferredColor(),
                request.preferredMaterial(),
                request.preferredSize()
        );

        return NeedsAnalysisResultResponse.from(needsAnalysis);
    }

}

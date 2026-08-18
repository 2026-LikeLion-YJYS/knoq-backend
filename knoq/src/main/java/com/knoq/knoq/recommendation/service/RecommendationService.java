package com.knoq.knoq.recommendation.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.product.entity.Product;
import com.knoq.knoq.product.repository.ProductRepository;
import com.knoq.knoq.recommendation.dto.response.RecommendationResponse;
import com.knoq.knoq.recommendation.dto.response.RecommendedProductResponse;
import com.knoq.knoq.saved.entity.SavedProduct;
import com.knoq.knoq.saved.service.SavedProductService;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final int RECOMMENDATION_COUNT = 3;

    private final SessionRepository sessionRepository;
    private final ProductRepository productRepository;
    private final SavedProductService savedProductService;
    private final RecommendationRuleMatcher ruleMatcher;

    @Transactional
    public RecommendationResponse recommend(String sessionId) {
        Session session = findValidSession(sessionId);
        // TODO(A 협의): FR-103은 실제 사용자 동작이므로 공통 세션 만료시간 갱신 방식이 확정되면 연결한다.

        List<ScoredProduct> recommendations = productRepository.findAll().stream()
                .distinct()
                .map(product -> new ScoredProduct(
                        product,
                        ruleMatcher.match(product, session.getLifestyleTags())
                ))
                .sorted(Comparator
                        .comparingInt((ScoredProduct recommendation) -> recommendation.matchResult().score())
                        .reversed()
                        .thenComparing(recommendation -> recommendation.product().getId()))
                .limit(RECOMMENDATION_COUNT)
                .toList();

        List<RecommendedProductResponse> products = recommendations.stream()
                .map(recommendation -> toResponse(sessionId, recommendation))
                .toList();

        return new RecommendationResponse(
                ruleMatcher.createSummary(session.getLifestyleTags(), products.size()),
                products
        );
    }

    private RecommendedProductResponse toResponse(String sessionId, ScoredProduct recommendation) {
        SavedProduct savedProduct = savedProductService.saveFromRecommend(
                sessionId,
                recommendation.product().getId()
        );

        return new RecommendedProductResponse(
                "sav_" + savedProduct.getId(),
                savedProduct.getProductId(),
                recommendation.matchResult().reason()
        );
    }

    private Session findValidSession(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(ErrorCode.SESSION_NOT_FOUND));
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.SESSION_EXPIRED);
        }
        return session;
    }

    private record ScoredProduct(
            Product product,
            RecommendationRuleMatcher.MatchResult matchResult
    ) {
    }
}

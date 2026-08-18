package com.knoq.knoq.recommendation.service;

import com.knoq.knoq.product.entity.Product;
import com.knoq.knoq.product.repository.ProductRepository;
import com.knoq.knoq.recommendation.dto.response.RecommendationResponse;
import com.knoq.knoq.recommendation.dto.response.RecommendedProductResponse;
import com.knoq.knoq.saved.entity.SavedProduct;
import com.knoq.knoq.saved.service.SavedProductService;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.service.SessionExpirationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final int RECOMMENDATION_COUNT = 3;

    private final SessionExpirationService sessionExpirationService;
    private final ProductRepository productRepository;
    private final SavedProductService savedProductService;
    private final RecommendationRuleMatcher ruleMatcher;

    @Transactional
    public RecommendationResponse recommend(String sessionId) {
        Session session = sessionExpirationService.getValidSessionAndRefresh(sessionId);

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

    private record ScoredProduct(
            Product product,
            RecommendationRuleMatcher.MatchResult matchResult
    ) {
    }
}

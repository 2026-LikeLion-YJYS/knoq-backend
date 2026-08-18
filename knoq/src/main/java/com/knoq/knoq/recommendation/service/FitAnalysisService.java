package com.knoq.knoq.recommendation.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.product.entity.Product;
import com.knoq.knoq.product.repository.ProductRepository;
import com.knoq.knoq.recommendation.dto.response.FitAnalysisResponse;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.service.SessionExpirationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FitAnalysisService {

    private final SessionExpirationService sessionExpirationService;
    private final ProductRepository productRepository;
    private final FitAnalysisGenerator fitAnalysisGenerator;
    private final RecommendationRuleMatcher recommendationRuleMatcher;

    @Transactional
    public FitAnalysisResponse analyze(String sessionId, String productId) {
        Session session = sessionExpirationService.getValidSessionAndRefresh(sessionId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));

        FitAnalysisResponse generated = fitAnalysisGenerator.generate(session.getLifestyleTags(), product);
        return generated != null ? generated : fallback(session, product);
    }

    private FitAnalysisResponse fallback(Session session, Product product) {
        RecommendationRuleMatcher.MatchResult match =
                recommendationRuleMatcher.match(product, session.getLifestyleTags());
        String tags = session.getLifestyleTags().stream()
                .map(Enum::name)
                .reduce((left, right) -> left + ", " + right)
                .orElse("선택한");

        if (match.score() > 0) {
            return new FitAnalysisResponse(
                    tags + " 라이프스타일과 제품 속성을 비교한 결과 잘 어울리는 요소가 있어요.",
                    List.of(match.reason()),
                    List.of("실제 색상과 소재는 매장에서 직접 확인해 주세요.")
            );
        }

        return new FitAnalysisResponse(
                tags + " 라이프스타일과 제품 속성을 함께 비교해 보았어요.",
                List.of("제품의 기본 속성을 기준으로 취향과의 적합성을 확인할 수 있어요."),
                List.of("라이프스타일 태그와 직접 일치하는 속성이 적어 실제 제품을 추가로 확인해 주세요.")
        );
    }

}

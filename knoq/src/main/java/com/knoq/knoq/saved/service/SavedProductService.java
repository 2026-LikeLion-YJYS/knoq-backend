package com.knoq.knoq.saved.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.saved.entity.SavedProduct;
import com.knoq.knoq.saved.repository.SavedProductRepository;
import com.knoq.knoq.saved.entity.SavedProductSource;
import com.knoq.knoq.sessions.service.SessionExpirationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedProductService {

    // TODO: 보관함 저장 상한 확정 후 수정
    private static final int MAX_SAVED = 9;

    private final SavedProductRepository repository;
    private final SessionExpirationService sessionExpirationService;

    @Transactional
    public SavedProduct saveFromCamera(
            String sessionId,
            String productId
    ) {
        return save(
                sessionId,
                productId,
                SavedProductSource.CAMERA
        );
    }

    @Transactional
    public SavedProduct saveFromRecommend(
            String sessionId,
            String productId
    ) {
        return save(
                sessionId,
                productId,
                SavedProductSource.RECOMMEND
        );
    }

    private SavedProduct save(
            String sessionId,
            String productId,
            SavedProductSource source
    ) {
        sessionExpirationService.getValidSessionAndRefresh(sessionId);

        return repository
                .findBySessionIdAndProductId(sessionId, productId)
                .orElseGet(() -> {

                    if (repository.countBySessionId(sessionId) >= MAX_SAVED) {
                        throw new ApiException(ErrorCode.SAVED_PRODUCT_LIMIT_EXCEEDED);
                    }

                    SavedProduct savedProduct =
                            SavedProduct.of(
                                    sessionId,
                                    productId,
                                    source
                            );

                    return repository.save(savedProduct);
                });
    }

    @Transactional
    public List<SavedProduct> findAll(String sessionId) {
        sessionExpirationService.getValidSessionAndRefresh(sessionId);
        return repository
                .findBySessionIdOrderBySavedAtDesc(sessionId);
    }

    @Transactional
    public void delete(
            String sessionId,
            String productId
    ) {
        sessionExpirationService.getValidSessionAndRefresh(sessionId);

        SavedProduct savedProduct =
                repository
                        .findBySessionIdAndProductId(
                                sessionId,
                                productId
                        )
                        .orElseThrow(() ->
                                new ApiException(ErrorCode.SAVED_PRODUCT_NOT_FOUND)
                        );

        repository.delete(savedProduct);
    }

    @Transactional
    public void deleteAll(String sessionId) {
        repository.deleteBySessionId(sessionId);
    }
}

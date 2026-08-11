package com.knoq.knoq.saved;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedProductService {

    private static final int MAX_SAVED = 9;   // TODO 상한 확정 필요

    private final SavedProductRepository repository;

    @Transactional
    public SavedProduct saveFromCamera(String sessionId, String productId) {
        return save(sessionId, productId, SavedProductSource.CAMERA);
    }

    @Transactional
    public SavedProduct saveFromRecommend(String sessionId, String productId) {
        return save(sessionId, productId, SavedProductSource.RECOMMEND);
    }

    private SavedProduct save(String sessionId, String productId, SavedProductSource source) {
        return repository.findBySessionIdAndProductId(sessionId, productId)
                .orElseGet(() -> {
                    if (repository.countBySessionId(sessionId) >= MAX_SAVED) {
                        throw new IllegalStateException("SAVED_PRODUCT_LIMIT_EXCEEDED");
                    }
                    SavedProduct saved = (source == SavedProductSource.CAMERA)
                            ? SavedProduct.ofCamera(sessionId, productId)
                            : SavedProduct.ofRecommend(sessionId, productId);
                    return repository.save(saved);
                });
    }

    @Transactional(readOnly = true)
    public List<SavedProduct> findAll(String sessionId) {
        return repository.findBySessionIdOrderBySavedAtDesc(sessionId);
    }

    @Transactional
    public void delete(String sessionId, String productId) {
        SavedProduct saved = repository.findBySessionIdAndProductId(sessionId, productId)
                .orElseThrow(() -> new IllegalArgumentException("SAVED_PRODUCT_NOT_FOUND"));
        repository.delete(saved);
    }

    @Transactional
    public void deleteAll(String sessionId) {
        repository.deleteBySessionId(sessionId);
    }
}
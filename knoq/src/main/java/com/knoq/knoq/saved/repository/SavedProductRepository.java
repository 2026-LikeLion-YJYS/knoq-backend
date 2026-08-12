package com.knoq.knoq.saved.repository;

import com.knoq.knoq.saved.entity.SavedProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedProductRepository
        extends JpaRepository<SavedProduct, Long> {

    List<SavedProduct> findBySessionIdOrderBySavedAtDesc(
            String sessionId
    );

    Optional<SavedProduct> findBySessionIdAndProductId(
            String sessionId,
            String productId
    );

    long countBySessionId(String sessionId);

    void deleteBySessionId(String sessionId);
}
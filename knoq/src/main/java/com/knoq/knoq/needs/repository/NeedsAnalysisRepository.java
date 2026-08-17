package com.knoq.knoq.needs.repository;

import com.knoq.knoq.needs.entity.NeedsAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NeedsAnalysisRepository extends JpaRepository<NeedsAnalysis, Long> {
    Optional<NeedsAnalysis> findBySessionId(String sessionId);
}
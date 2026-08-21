package com.knoq.knoq.consultation.repository;

import com.knoq.knoq.consultation.entity.ConsultationRequest;
import com.knoq.knoq.consultation.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ConsultationRequestRepository extends JpaRepository<ConsultationRequest, String> {
    boolean existsBySessionIdAndStatusIn(String sessionId, Collection<RequestStatus> statuses);

    List<ConsultationRequest> findAllByStoreIdOrderByRequestedAtDesc(Long storeId);
}

package com.knoq.knoq.consultation.service;

import com.knoq.knoq.consultation.dto.request.CreateConsultationRequest;
import com.knoq.knoq.consultation.dto.response.CreateConsultationResponse;
import com.knoq.knoq.consultation.dto.response.ConsultationStatusResponse;
import com.knoq.knoq.consultation.entity.ConsultationRequest;
import com.knoq.knoq.consultation.entity.HelpType;
import com.knoq.knoq.consultation.entity.RequestStatus;
import com.knoq.knoq.consultation.repository.ConsultationRequestRepository;
import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.global.util.IdGenerator;
import com.knoq.knoq.notification.service.NotificationService;
import com.knoq.knoq.product.repository.ProductRepository;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.service.SessionExpirationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationRequestService {

    private static final int MAX_PRODUCTS = 3;
    private static final List<RequestStatus> ACTIVE_STATUSES = List.of(
            RequestStatus.REQUESTED,
            RequestStatus.ACCEPTED,
            RequestStatus.IN_PROGRESS
    );

    private final ConsultationRequestRepository consultationRequestRepository;
    private final SessionExpirationService sessionExpirationService;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Transactional
    public CreateConsultationResponse create(String sessionId, CreateConsultationRequest request) {
        Session session = sessionExpirationService.getValidSessionAndRefresh(sessionId);
        validateRequest(request);
        validateNoActiveRequest(sessionId);
        validateProducts(request.productIds());

        ConsultationRequest consultationRequest = ConsultationRequest.of(
                IdGenerator.generate("req"), sessionId, session.getStoreId(), request.helpType(),
                request.includeNeedsAnalysis()
        );
        request.productIds().forEach(consultationRequest::addProduct);

        ConsultationRequest savedRequest = consultationRequestRepository.saveAndFlush(consultationRequest);
        notificationService.createRequested(savedRequest);

        return CreateConsultationResponse.from(savedRequest);
    }

    @Transactional(readOnly = true)
    public ConsultationStatusResponse getStatus(String sessionId, String requestId) {
        sessionExpirationService.getValidSession(sessionId);

        ConsultationRequest consultationRequest = consultationRequestRepository.findById(requestId)
                .filter(request -> request.getSessionId().equals(sessionId))
                .orElseThrow(() -> new ApiException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));

        return ConsultationStatusResponse.from(consultationRequest);
    }

    private void validateRequest(CreateConsultationRequest request) {
        if (request == null || request.helpType() == null || request.productIds() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        if (request.productIds().size() > MAX_PRODUCTS) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        if (request.helpType() == HelpType.PRODUCT_COMPARISON && request.productIds().size() < 2) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        if (request.productIds().stream().anyMatch(id -> id == null || id.isBlank())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateNoActiveRequest(String sessionId) {
        if (consultationRequestRepository.existsBySessionIdAndStatusIn(sessionId, ACTIVE_STATUSES)) {
            throw new ApiException(ErrorCode.ACTIVE_CONSULTATION_REQUEST_EXISTS);
        }
    }

    private void validateProducts(List<String> productIds) {
        for (String productId : productIds) {
            if (!productRepository.existsById(productId)) {
                throw new ApiException(ErrorCode.PRODUCT_NOT_FOUND);
            }
        }
    }
}

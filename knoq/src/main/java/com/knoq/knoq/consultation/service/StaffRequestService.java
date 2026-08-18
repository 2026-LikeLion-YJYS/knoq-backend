package com.knoq.knoq.consultation.service;

import com.knoq.knoq.consultation.dto.request.UpdateConsultationStatusRequest;
import com.knoq.knoq.consultation.dto.response.StaffRequestDetailResponse;
import com.knoq.knoq.consultation.dto.response.StaffRequestInboxResponse;
import com.knoq.knoq.consultation.dto.response.StaffRequestSummaryResponse;
import com.knoq.knoq.consultation.dto.response.UpdateConsultationStatusResponse;
import com.knoq.knoq.consultation.entity.ConsultationRequest;
import com.knoq.knoq.consultation.entity.RequestStatus;
import com.knoq.knoq.consultation.repository.ConsultationRequestRepository;
import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.needs.dto.response.NeedsAnalysisSummary;
import com.knoq.knoq.needs.repository.NeedsAnalysisRepository;
import com.knoq.knoq.product.dto.ProductDetailResponse;
import com.knoq.knoq.product.service.ProductService;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.repository.SessionRepository;
import com.knoq.knoq.staff.jwt.StaffTokenProvider;
import com.knoq.knoq.store.entity.Store;
import com.knoq.knoq.store.repository.StoreRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffRequestService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final ConsultationRequestRepository consultationRequestRepository;
    private final SessionRepository sessionRepository;
    private final NeedsAnalysisRepository needsAnalysisRepository;
    private final ProductService productService;
    private final StoreRepository storeRepository;
    private final StaffTokenProvider staffTokenProvider;

    @Transactional(readOnly = true)
    public StaffRequestInboxResponse findAll(String authorizationHeader) {
        Store store = authenticateStore(authorizationHeader);

        List<StaffRequestSummaryResponse> requests =
                consultationRequestRepository.findAllByStoreIdOrderByRequestedAtDesc(store.getId())
                        .stream()
                        .map(request -> StaffRequestSummaryResponse.of(
                                request,
                                findSession(request.getSessionId())
                        ))
                        .toList();

        return new StaffRequestInboxResponse(requests);
    }

    @Transactional(readOnly = true)
    public StaffRequestDetailResponse findDetail(String authorizationHeader, String requestId) {
        Store store = authenticateStore(authorizationHeader);
        ConsultationRequest request = findStoreRequest(store.getId(), requestId);
        Session session = findSession(request.getSessionId());

        List<ProductDetailResponse> products = request.getProducts().stream()
                .map(product -> productService.getProductDetail(product.getProductId()))
                .toList();

        NeedsAnalysisSummary needsAnalysis = request.isIncludeNeedsAnalysis()
                ? needsAnalysisRepository.findBySessionId(request.getSessionId())
                        .map(NeedsAnalysisSummary::from)
                        .orElse(null)
                : null;

        return new StaffRequestDetailResponse(
                request.getId(),
                session.getNickname(),
                request.getHelpType(),
                List.copyOf(session.getLifestyleTags()),
                products,
                needsAnalysis,
                request.getStatus()
        );
    }

    @Transactional
    public UpdateConsultationStatusResponse updateStatus(
            String authorizationHeader,
            String requestId,
            UpdateConsultationStatusRequest statusRequest
    ) {
        Store store = authenticateStore(authorizationHeader);
        ConsultationRequest consultationRequest = findStoreRequest(store.getId(), requestId);

        if (statusRequest == null
                || statusRequest.status() == null
                || !isValidTransition(consultationRequest.getStatus(), statusRequest.status())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        consultationRequest.updateStatus(statusRequest.status());
        return UpdateConsultationStatusResponse.from(consultationRequest);
    }

    private Store authenticateStore(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());
        Claims claims;
        try {
            claims = staffTokenProvider.parseToken(token);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        String storeCode = claims.get("storeCode", String.class);
        if (storeCode == null || storeCode.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        return storeRepository.findByStoreCode(storeCode)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
    }

    private Session findSession(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(ErrorCode.SESSION_NOT_FOUND));
    }

    private ConsultationRequest findStoreRequest(Long storeId, String requestId) {
        return consultationRequestRepository.findById(requestId)
                .filter(request -> request.getStoreId().equals(storeId))
                .orElseThrow(() -> new ApiException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
    }

    private boolean isValidTransition(RequestStatus currentStatus, RequestStatus nextStatus) {
        return switch (currentStatus) {
            case REQUESTED -> nextStatus == RequestStatus.ACCEPTED;
            case ACCEPTED -> nextStatus == RequestStatus.IN_PROGRESS;
            case IN_PROGRESS -> nextStatus == RequestStatus.COMPLETED;
            case COMPLETED, EXPIRED -> false;
        };
    }
}

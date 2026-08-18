package com.knoq.knoq.notification.service;

import com.knoq.knoq.consultation.entity.ConsultationRequest;
import com.knoq.knoq.consultation.entity.RequestStatus;
import com.knoq.knoq.global.util.IdGenerator;
import com.knoq.knoq.notification.dto.response.NotificationListResponse;
import com.knoq.knoq.notification.dto.response.NotificationResponse;
import com.knoq.knoq.notification.entity.Notification;
import com.knoq.knoq.notification.repository.NotificationRepository;
import com.knoq.knoq.sessions.event.SessionFinishedEvent;
import com.knoq.knoq.sessions.service.SessionExpirationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    static final String REQUESTED_MESSAGE =
            "상담 요청이 전달되었어요. 탐색하신 제품과 요청 내용을 어드바이저에게 전달했습니다.";
    static final String ACCEPTED_CONFIRMED_MESSAGE =
            "어드바이저가 확인했어요. 고객님이 살펴본 제품과 요청 내용을 확인하고 있습니다.";
    static final String ACCEPTED_COMING_MESSAGE =
            "어드바이저가 오고 있어요. 잠시만 기다려주세요.";
    static final String IN_PROGRESS_MESSAGE =
            "상담이 시작됐어요. 고객님의 고민을 해결해드릴게요.";
    static final String COMPLETED_MESSAGE =
            "상담이 종료됐어요. 감사합니다.";

    private final NotificationRepository notificationRepository;
    private final SessionExpirationService sessionExpirationService;

    @Transactional
    public void createRequested(ConsultationRequest request) {
        save(request, RequestStatus.REQUESTED, REQUESTED_MESSAGE);
    }

    @Transactional
    public void createForStatusChange(ConsultationRequest request) {
        switch (request.getStatus()) {
            case ACCEPTED -> {
                save(request, RequestStatus.ACCEPTED, ACCEPTED_CONFIRMED_MESSAGE);
                save(request, RequestStatus.ACCEPTED, ACCEPTED_COMING_MESSAGE);
            }
            case IN_PROGRESS -> save(request, RequestStatus.IN_PROGRESS, IN_PROGRESS_MESSAGE);
            case COMPLETED -> save(request, RequestStatus.COMPLETED, COMPLETED_MESSAGE);
            case REQUESTED, EXPIRED -> {
                // 직원 상태 변경 API에서 생성하지 않는 상태
            }
        }
    }

    @Transactional(readOnly = true)
    public NotificationListResponse findAll(String sessionId) {
        sessionExpirationService.getValidSession(sessionId);

        return new NotificationListResponse(
                notificationRepository.findAllBySessionIdOrderByCreatedAtDesc(sessionId)
                        .stream()
                        .map(NotificationResponse::from)
                        .toList()
        );
    }

    @EventListener
    @Transactional
    public void deleteAllOnSessionFinished(SessionFinishedEvent event) {
        notificationRepository.deleteAllBySessionId(event.sessionId());
    }

    private void save(ConsultationRequest request, RequestStatus status, String message) {
        notificationRepository.save(Notification.of(
                IdGenerator.generate("ntf"),
                request.getSessionId(),
                request.getId(),
                status,
                message
        ));
    }

}

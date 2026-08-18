package com.knoq.knoq.notification;

import com.knoq.knoq.consultation.dto.request.CreateConsultationRequest;
import com.knoq.knoq.consultation.dto.request.UpdateConsultationStatusRequest;
import com.knoq.knoq.consultation.dto.response.CreateConsultationResponse;
import com.knoq.knoq.consultation.entity.HelpType;
import com.knoq.knoq.consultation.entity.RequestStatus;
import com.knoq.knoq.consultation.service.ConsultationRequestService;
import com.knoq.knoq.consultation.service.StaffRequestService;
import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.notification.dto.response.NotificationListResponse;
import com.knoq.knoq.notification.entity.Notification;
import com.knoq.knoq.notification.repository.NotificationRepository;
import com.knoq.knoq.notification.service.NotificationService;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.repository.SessionRepository;
import com.knoq.knoq.sessions.service.SessionService;
import com.knoq.knoq.staff.jwt.StaffTokenProvider;
import com.knoq.knoq.store.entity.Store;
import com.knoq.knoq.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ConsultationRequestService consultationRequestService;

    @Autowired
    private StaffRequestService staffRequestService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StaffTokenProvider staffTokenProvider;

    private Session session;
    private Store store;
    private String authorizationHeader;
    private String testSuffix;

    @BeforeEach
    void setUp() {
        testSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        store = storeRepository.save(Store.of("NTF-" + testSuffix, "알림 테스트 매장"));
        session = saveSession("sess_ntf_" + testSuffix, store.getId(), 30);
        authorizationHeader = "Bearer " + staffTokenProvider.generateToken(
                "staff_ntf_" + testSuffix,
                store.getStoreCode()
        );
    }

    @Test
    @DisplayName("상담 요청을 생성하면 REQUESTED 알림과 생성시각이 저장된다")
    void create_request_creates_requested_notification() {
        CreateConsultationResponse created = createConsultation(session.getId());

        NotificationListResponse response = notificationService.findAll(session.getId());

        assertThat(created.requestedAt()).isNotNull();
        assertThat(response.notifications()).singleElement().satisfies(notification -> {
            assertThat(notification.notificationId()).startsWith("ntf_");
            assertThat(notification.requestId()).isEqualTo(created.requestId());
            assertThat(notification.status()).isEqualTo(RequestStatus.REQUESTED);
            assertThat(notification.message()).isEqualTo(
                    "상담 요청이 전달되었어요. 탐색하신 제품과 요청 내용을 어드바이저에게 전달했습니다."
            );
            assertThat(notification.createdAt()).isNotNull();
        });
    }

    @Test
    @DisplayName("ACCEPTED로 변경하면 확인 및 이동 알림 두 건이 생성된다")
    void accepted_creates_two_notifications() {
        CreateConsultationResponse created = createConsultation(session.getId());

        staffRequestService.updateStatus(
                authorizationHeader,
                created.requestId(),
                new UpdateConsultationStatusRequest(RequestStatus.ACCEPTED)
        );

        List<Notification> acceptedNotifications = notificationRepository
                .findAllBySessionIdOrderByCreatedAtDesc(session.getId())
                .stream()
                .filter(notification -> notification.getStatus() == RequestStatus.ACCEPTED)
                .toList();

        assertThat(acceptedNotifications).hasSize(2);
        assertThat(acceptedNotifications)
                .extracting(Notification::getMessage)
                .containsExactlyInAnyOrder(
                        "어드바이저가 확인했어요. 고객님이 살펴본 제품과 요청 내용을 확인하고 있습니다.",
                        "어드바이저가 오고 있어요. 잠시만 기다려주세요."
                );
    }

    @Test
    @DisplayName("IN_PROGRESS와 COMPLETED 변경마다 명세에 맞는 알림이 한 건씩 생성된다")
    void in_progress_and_completed_create_notifications() {
        CreateConsultationResponse created = createConsultation(session.getId());
        updateStatus(created.requestId(), RequestStatus.ACCEPTED);
        updateStatus(created.requestId(), RequestStatus.IN_PROGRESS);
        updateStatus(created.requestId(), RequestStatus.COMPLETED);

        List<Notification> notifications =
                notificationRepository.findAllBySessionIdOrderByCreatedAtDesc(session.getId());

        assertThat(notifications)
                .filteredOn(notification -> notification.getStatus() == RequestStatus.IN_PROGRESS)
                .singleElement()
                .satisfies(notification -> assertThat(notification.getMessage())
                        .isEqualTo("상담이 시작됐어요. 고객님의 고민을 해결해드릴게요."));
        assertThat(notifications)
                .filteredOn(notification -> notification.getStatus() == RequestStatus.COMPLETED)
                .singleElement()
                .satisfies(notification -> assertThat(notification.getMessage())
                        .isEqualTo("상담이 종료됐어요. 감사합니다."));
    }

    @Test
    @DisplayName("알림 목록은 최신순이며 다른 세션의 알림을 반환하지 않는다")
    void find_all_returns_only_own_notifications_in_latest_order() {
        CreateConsultationResponse created = createConsultation(session.getId());
        updateStatus(created.requestId(), RequestStatus.ACCEPTED);

        Session anotherSession = saveSession("sess_ntf_other_" + testSuffix, store.getId(), 30);
        notificationRepository.save(Notification.of(
                "ntf_other_" + testSuffix,
                anotherSession.getId(),
                "req_other_" + testSuffix,
                RequestStatus.REQUESTED,
                "다른 세션 알림"
        ));

        NotificationListResponse response = notificationService.findAll(session.getId());

        assertThat(response.notifications()).hasSize(3);
        assertThat(response.notifications())
                .allMatch(notification -> notification.requestId().equals(created.requestId()));
        assertThat(response.notifications())
                .extracting(notification -> notification.createdAt())
                .isSortedAccordingTo((left, right) -> right.compareTo(left));
    }

    @Test
    @DisplayName("알림 폴링 조회는 세션 만료시각을 갱신하지 않는다")
    void find_all_does_not_extend_session_expiration() {
        createConsultation(session.getId());
        LocalDateTime expiresAtBeforePolling = session.getExpiresAt();

        notificationService.findAll(session.getId());

        LocalDateTime expiresAtAfterPolling = sessionRepository.findById(session.getId())
                .orElseThrow()
                .getExpiresAt();
        assertThat(expiresAtAfterPolling).isEqualTo(expiresAtBeforePolling);
    }

    @Test
    @DisplayName("만료된 세션으로 알림을 조회하면 410 예외가 발생한다")
    void find_all_fails_when_session_is_expired() {
        Session expiredSession = saveSession("sess_ntf_expired_" + testSuffix, store.getId(), -1);

        assertThatThrownBy(() -> notificationService.findAll(expiredSession.getId()))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_EXPIRED);
    }

    @Test
    @DisplayName("쇼핑을 마치면 해당 세션의 알림이 모두 삭제된다")
    void finish_shopping_deletes_notifications() {
        createConsultation(session.getId());
        assertThat(notificationRepository.findAllBySessionIdOrderByCreatedAtDesc(session.getId()))
                .isNotEmpty();

        sessionService.finishShopping(session.getId());

        assertThat(notificationRepository.findAllBySessionIdOrderByCreatedAtDesc(session.getId()))
                .isEmpty();
    }

    private CreateConsultationResponse createConsultation(String sessionId) {
        return consultationRequestService.create(
                sessionId,
                new CreateConsultationRequest(
                        HelpType.PRODUCT_RECOMMENDATION,
                        List.of(),
                        false
                )
        );
    }

    private void updateStatus(String requestId, RequestStatus status) {
        staffRequestService.updateStatus(
                authorizationHeader,
                requestId,
                new UpdateConsultationStatusRequest(status)
        );
    }

    private Session saveSession(String id, Long storeId, long expiresInMinutes) {
        return sessionRepository.save(Session.of(
                id,
                "token_" + id,
                storeId,
                LocalDateTime.now().plusMinutes(expiresInMinutes)
        ));
    }
}

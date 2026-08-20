package com.knoq.knoq.sessions.service;

import com.knoq.knoq.account.entity.Account;
import com.knoq.knoq.account.repository.AccountRepository;
import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.product.entity.Product;
import com.knoq.knoq.product.repository.ProductRepository;
import com.knoq.knoq.saved.entity.SavedProduct;
import com.knoq.knoq.saved.repository.SavedProductRepository;
import com.knoq.knoq.saved.service.SavedProductService;
import com.knoq.knoq.sessions.client.KakaoApiClient;
import com.knoq.knoq.sessions.dto.ConsentRequest;
import com.knoq.knoq.sessions.dto.ConsentType;
import com.knoq.knoq.sessions.dto.ConsentsResponse;
import com.knoq.knoq.sessions.dto.CreateSessionRequest;
import com.knoq.knoq.sessions.dto.CreateSessionResponse;
import com.knoq.knoq.sessions.dto.GetSessionResponse;
import com.knoq.knoq.sessions.dto.KakaoLoginRequest;
import com.knoq.knoq.sessions.dto.KakaoLoginResponse;
import com.knoq.knoq.sessions.dto.LifestyleTagsRequest;
import com.knoq.knoq.sessions.dto.LifestyleTagsResponse;
import com.knoq.knoq.sessions.dto.NicknameRequest;
import com.knoq.knoq.sessions.dto.NicknameResponse;
import com.knoq.knoq.sessions.dto.SessionArchiveResponse;
import com.knoq.knoq.sessions.dto.StorageScopeRequest;
import com.knoq.knoq.sessions.dto.StorageScopeResponse;
import com.knoq.knoq.sessions.entity.LifestyleTag;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.entity.StorageScope;
import com.knoq.knoq.sessions.event.SessionFinishedEvent;
import com.knoq.knoq.sessions.repository.SessionRepository;
import com.knoq.knoq.store.entity.Store;
import com.knoq.knoq.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {
    private static final String ID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    // 탐색 아카이브 하루 1개 정책의 "하루" 경계 기준(한국시간 자정)
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // FR-101: 닉네임 관련 — 데모용으로 간단히 준비한 목록
    private static final List<String> FORBIDDEN_WORDS = List.of("시발", "씨발", "개새끼", "병신", "관리자", "admin");
    private static final String[] NICKNAME_ADJECTIVES = {"민트", "핑크", "레몬", "코코", "블루", "라일락", "구름", "노을"};
    private static final String[] NICKNAME_NOUNS = {"라떼", "젤리", "토끼", "고양이", "별빛", "구슬", "머핀", "파도"};

    private final SessionRepository sessionRepository;
    private final StoreRepository storeRepository;
    private final AccountRepository accountRepository;
    private final SavedProductRepository savedProductRepository;
    private final SavedProductService savedProductService;
    private final ProductRepository productRepository;
    private final KakaoApiClient kakaoApiClient;
    private final ApplicationEventPublisher eventPublisher;
    private final SessionExpirationService sessionExpirationService;

    // application.yml에 knoq.session.expiry-minutes가 없으면 기본값 60을 씀
    @Value("${knoq.session.expiry-minutes:60}")
    private long expiryMinutes;

    @Transactional
    public CreateSessionResponse createSession(CreateSessionRequest request) {
        // 1. storeCode로 매장 찾기. 없으면 여기서 바로 에러를 던지고 끝냄 (세션은 만들지 않음)
        Store store = storeRepository.findByStoreCode(request.storeCode())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_STORE_CODE));

        // 2. 세션 만들기
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes);
        Session session = Session.of(
                generateSessionId(),
                generateToken(),
                store.getId(),
                expiresAt
        );

        // 3. DB에 저장
        sessionRepository.save(session);

        // 4. 응답 형태로 바꿔서 반환 (storeName은 Store에서 가져옴)
        return new CreateSessionResponse(
                session.getId(),
                session.getToken(),
                store.getStoreName(),
                session.getExpiresAt()
        );
    }

    @Transactional(readOnly = true)
    public GetSessionResponse getSession(String sessionId) {
        // FR-401은 폴링 성격의 조회이므로 유효성만 확인하고 만료시간은 갱신하지 않는다.
        Session session = sessionExpirationService.getValidSession(sessionId);

        // storeId로 매장 이름 찾기
        Store store = storeRepository.findById(session.getStoreId())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_STORE_CODE));

        return new GetSessionResponse(
                session.getId(),
                store.getStoreName(),
                session.getStorageScope(),
                session.getNickname(),
                session.getLifestyleTags(),
                session.getExpiresAt()
        );
    }

    @Transactional(readOnly = true)
    public SessionArchiveResponse getArchive(String currentSessionId) {
        Session currentSession = sessionExpirationService.getValidSession(currentSessionId);
        if (currentSession.getStorageScope() != StorageScope.ACCOUNT
                || currentSession.getAccountId() == null) {
            throw new ApiException(ErrorCode.ACCOUNT_LOGIN_REQUIRED);
        }

        List<Session> sessions = sessionRepository
                .findByAccountIdOrderByCreatedAtDesc(currentSession.getAccountId());
        List<String> sessionIds = sessions.stream().map(Session::getId).toList();

        Map<String, List<SavedProduct>> savedProductsBySession = sessionIds.isEmpty()
                ? Collections.emptyMap()
                : savedProductRepository.findBySessionIdInOrderBySavedAtDesc(sessionIds).stream()
                .collect(Collectors.groupingBy(SavedProduct::getSessionId));

        List<String> productIds = savedProductsBySession.values().stream()
                .flatMap(List::stream)
                .map(SavedProduct::getProductId)
                .distinct()
                .toList();
        Map<String, Product> productsById = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<SessionArchiveResponse.Visit> visits = sessions.stream()
                .map(session -> new SessionArchiveResponse.Visit(
                        session.getId(),
                        session.getCreatedAt(),
                        session.getId().equals(currentSessionId),
                        toArchivedProducts(
                                savedProductsBySession.getOrDefault(session.getId(), List.of()),
                                productsById
                        )
                ))
                .toList();

        return new SessionArchiveResponse(visits.size(), visits);
    }

    private List<SessionArchiveResponse.ArchivedProduct> toArchivedProducts(
            List<SavedProduct> savedProducts,
            Map<String, Product> productsById
    ) {
        return savedProducts.stream()
                .map(savedProduct -> {
                    Product product = productsById.get(savedProduct.getProductId());
                    return new SessionArchiveResponse.ArchivedProduct(
                            savedProduct.getProductId(),
                            product == null ? null : product.getName(),
                            product == null ? null : product.getThumbnailUrl(),
                            savedProduct.getSource(),
                            savedProduct.getSavedAt()
                    );
                })
                .toList();
    }

    @Transactional
    public ConsentsResponse agreeConsents(String sessionId, ConsentRequest request) {
        Session session = sessionExpirationService.getValidSessionAndRefresh(sessionId);

        // 필수 3개(이용약관·개인정보·만14세) 중 하나라도 false면 400
        if (!request.termsOfService() || !request.privacyPolicy() || !request.over14()) {
            throw new ApiException(ErrorCode.CONSENT_REQUIRED);
        }

        LocalDateTime consentedAt = LocalDateTime.now();
        session.agreeConsents(
                request.termsOfService(),
                request.privacyPolicy(),
                request.over14(),
                request.marketingOptIn(),
                consentedAt
        );
        // sessionRepository.save() 호출 안 함: session은 findValidSession에서 findById로 가져온
        // "영속 상태" 객체라서, @Transactional 메서드가 끝날 때 바뀐 값이 자동으로 DB에 반영됨 (더티 체킹)

        return new ConsentsResponse(List.of(
                new ConsentsResponse.ConsentItem(ConsentType.TERMS_OF_SERVICE, request.termsOfService(), consentedAt),
                new ConsentsResponse.ConsentItem(ConsentType.PRIVACY_POLICY, request.privacyPolicy(), consentedAt),
                new ConsentsResponse.ConsentItem(ConsentType.OVER14, request.over14(), consentedAt),
                new ConsentsResponse.ConsentItem(ConsentType.MARKETING_OPT_IN, request.marketingOptIn(), consentedAt)
        ));
    }

    @Transactional
    public StorageScopeResponse selectStorageScope(String sessionId, StorageScopeRequest request) {
        Session session = sessionExpirationService.getValidSessionAndRefresh(sessionId);

        StorageScope requested = request.storageScope();
        // 클라이언트가 PENDING_KAKAO_LOGIN을 직접 보내는 건 허용 안 함 (시스템이 계산하는 상태값)
        if (requested == StorageScope.PENDING_KAKAO_LOGIN) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        if (requested == StorageScope.ACCOUNT) {
            // 아직 카카오 로그인 전이니 바로 ACCOUNT로 못 가고, "로그인 대기" 상태로만 바꿈
            session.requestAccountStorage();
            return new StorageScopeResponse(session.getStorageScope(), true);
        }

        session.usePrivateStorage();
        return new StorageScopeResponse(session.getStorageScope(), false);
    }

    @Transactional
    public KakaoLoginResponse kakaoLogin(String sessionId, KakaoLoginRequest request) {
        Session session = sessionExpirationService.getValidSessionAndRefresh(sessionId);

        Optional<Long> kakaoUserId = kakaoApiClient.getKakaoUserId(request.kakaoAccessToken());

        if (kakaoUserId.isEmpty()) {
            // 카카오 인증 실패해도 에러로 막지 않고, PRIVATE로 되돌린 뒤 200으로 응답
            session.usePrivateStorage();
            return new KakaoLoginResponse(
                    StorageScope.PRIVATE, null, session.getId(), session.getToken(), null, List.of(), false
            );
        }

        // 이 카카오 계정으로 이미 만들어진 Account가 있으면 재사용, 없으면 새로 생성
        Account account = accountRepository.findByKakaoUserId(kakaoUserId.get())
                .orElseGet(() -> accountRepository.save(Account.of(generateAccountId(), kakaoUserId.get())));

        // 탐색 아카이브 하루 1개 정책: 매장과 무관하게 같은 계정으로 "오늘"(한국시간 자정 기준) 이미 로그인한
        // 세션이 있으면 새 세션을 만들지 않고 그 세션으로 갈아탐 (온보딩도 다시 안 보여줌)
        Optional<Session> todaySession = sessionRepository
                .findFirstByAccountIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
                        account.getId(), todayStart()
                )
                .filter(existing -> !existing.getId().equals(session.getId()));

        if (todaySession.isPresent()) {
            return reuseTodaySession(session, todaySession.get(), account);
        }

        return startFirstLoginOfDay(session, account);
    }

    // 오늘의 두 번째 이상 로그인: 기존 일일 세션을 재사용하고, 방금 새로 만들어졌던 세션은 정리
    private KakaoLoginResponse reuseTodaySession(Session freshSession, Session todaySession, Account account) {
        // finishShopping()으로 이미 종료(즉시 만료)됐을 수 있어서, restoreFromPreviousSessions가 내부적으로
        // 유효성 검사를 하기 전에 먼저 만료시간을 되살려놔야 함
        todaySession.refreshExpiration(LocalDateTime.now().plusMinutes(expiryMinutes));

        // 방금 만들어진 세션 쪽에 저장된 게 있었다면(로그인 전에 잠깐 둘러본 경우) 재사용할 세션으로 옮겨줌
        savedProductService.restoreFromPreviousSessions(todaySession.getId(), List.of(freshSession.getId()));
        // 임시 세션을 지우기 전에 옮겨진 저장 제품 원본도 정리해 orphan row를 남기지 않는다.
        savedProductService.deleteAll(freshSession.getId());
        // 다른 매장으로 재진입한 경우 오늘 세션의 현재 매장을 갱신한다.
        todaySession.updateStore(freshSession.getStoreId());
        sessionRepository.delete(freshSession);

        return new KakaoLoginResponse(
                todaySession.getStorageScope(),
                account.getId(),
                todaySession.getId(),
                todaySession.getToken(),
                todaySession.getNickname(),
                todaySession.getLifestyleTags(),
                true // 재사용된 세션은 이미 오늘 온보딩을 마쳤으므로 항상 완료 상태
        );
    }

    // 오늘의 첫 로그인: 닉네임은 이전에 쓰던 값을 "추천값"으로만 채워주고(자동 확정 아님),
    // 라이프스타일 태그는 매일 새로 고르게 하므로 복원하지 않음
    private KakaoLoginResponse startFirstLoginOfDay(Session session, Account account) {
        List<Session> previousSessions = sessionRepository
                .findByAccountIdOrderByCreatedAtDesc(account.getId()).stream()
                .filter(previous -> !previous.getId().equals(session.getId()))
                .toList();

        String suggestedNickname = sessionRepository
                .findFirstByAccountIdAndNicknameIsNotNullOrderByCreatedAtDesc(account.getId())
                .map(Session::getNickname)
                .orElse(null);

        session.linkAccount(account.getId());
        savedProductService.restoreFromPreviousSessions(
                session.getId(),
                previousSessions.stream().map(Session::getId).toList()
        );

        return new KakaoLoginResponse(
                session.getStorageScope(),
                account.getId(),
                session.getId(),
                session.getToken(),
                suggestedNickname,
                List.of(),
                false
        );
    }

    private LocalDateTime todayStart() {
        return LocalDate.now(KST).atStartOfDay();
    }

    @Transactional
    public NicknameResponse setNickname(String sessionId, NicknameRequest request) {
        Session session = sessionExpirationService.getValidSessionAndRefresh(sessionId);

        String nickname = request.nickname();
        boolean autoGenerated = false;

        if (nickname == null || nickname.isBlank()) {
            nickname = generateRandomNickname();
            autoGenerated = true;
        } else if (containsForbiddenWord(nickname)) {
            throw new ApiException(ErrorCode.FORBIDDEN_WORD);
        }

        session.updateNickname(nickname);
        return new NicknameResponse(nickname, autoGenerated);
    }

    private boolean containsForbiddenWord(String nickname) {
        return FORBIDDEN_WORDS.stream().anyMatch(nickname::contains);
    }

    private String generateRandomNickname() {
        String adjective = NICKNAME_ADJECTIVES[RANDOM.nextInt(NICKNAME_ADJECTIVES.length)];
        String noun = NICKNAME_NOUNS[RANDOM.nextInt(NICKNAME_NOUNS.length)];
        return adjective + noun;
    }

    @Transactional
    public LifestyleTagsResponse updateLifestyleTags(String sessionId, LifestyleTagsRequest request) {
        Session session = sessionExpirationService.getValidSessionAndRefresh(sessionId);

        // @Valid로도 걸러지지만, 서비스 계층에서도 한 번 더 방어(직접 호출되는 경우 대비)
        List<LifestyleTag> tags = request.tags();
        if (tags == null || tags.isEmpty() || tags.size() > 3) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        session.updateLifestyleTags(tags);

        return new LifestyleTagsResponse(session.getLifestyleTags());
    }

    @Transactional
    public void finishShopping(String sessionId) {
        Session session = sessionExpirationService.getValidSession(sessionId);
        StorageScope storageScope = session.getStorageScope();

        if (storageScope == StorageScope.PRIVATE) {
            sessionRepository.delete(session); // 즉시 하드 삭제
        } else {
            session.endSession(); // ACCOUNT는 삭제 안 하고 즉시 만료 처리만
        }

        eventPublisher.publishEvent(new SessionFinishedEvent(sessionId, storageScope));
    }

    @Transactional
    public void logout(String sessionId) {
        Session session = sessionExpirationService.getValidSessionAndRefresh(sessionId);
        // "카카오 로그인"의 반대 동작 — 이미 만들어둔 usePrivateStorage()를 그대로 재사용
        // (storageScope를 PRIVATE로 되돌리고 accountId도 비움)
        session.usePrivateStorage();
    }

    private String generateSessionId() {
        StringBuilder sb = new StringBuilder("sess_");
        for (int i = 0; i < 12; i++) {
            sb.append(ID_CHARS.charAt(RANDOM.nextInt(ID_CHARS.length())));
        }
        return sb.toString();
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateAccountId() {
        StringBuilder sb = new StringBuilder("acct_");
        for (int i = 0; i < 12; i++) {
            sb.append(ID_CHARS.charAt(RANDOM.nextInt(ID_CHARS.length())));
        }
        return sb.toString();
    }
}

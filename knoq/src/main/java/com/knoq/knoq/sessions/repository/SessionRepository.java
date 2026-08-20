package com.knoq.knoq.sessions.repository;

import com.knoq.knoq.sessions.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, String> {

    List<Session> findByAccountIdOrderByCreatedAtDesc(String accountId);

    // 탐색 아카이브 하루 1개 정책: 같은 계정 + 같은 매장으로 오늘(자정 이후) 만들어진 세션이 있는지 조회
    Optional<Session> findFirstByAccountIdAndStoreIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            String accountId, Long storeId, LocalDateTime createdAtFrom
    );

    // 온보딩 닉네임 미리 채우기용: 매장 상관없이 이 계정이 마지막으로 쓴 닉네임을 찾음
    Optional<Session> findFirstByAccountIdAndNicknameIsNotNullOrderByCreatedAtDesc(String accountId);
}

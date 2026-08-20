package com.knoq.knoq.sessions.repository;

import com.knoq.knoq.sessions.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, String> {

    List<Session> findByAccountIdOrderByCreatedAtDesc(String accountId);

    // 탐색 아카이브 하루 1개 정책: 매장과 무관하게 같은 계정의 오늘 세션을 조회
    Optional<Session> findFirstByAccountIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            String accountId, LocalDateTime createdAtFrom
    );

    // 온보딩 닉네임 미리 채우기용: 매장 상관없이 이 계정이 마지막으로 쓴 닉네임을 찾음
    Optional<Session> findFirstByAccountIdAndNicknameIsNotNullOrderByCreatedAtDesc(String accountId);
}

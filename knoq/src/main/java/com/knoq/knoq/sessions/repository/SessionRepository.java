package com.knoq.knoq.sessions.repository;

import com.knoq.knoq.sessions.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, String> {

    List<Session> findByAccountIdOrderByCreatedAtDesc(String accountId);
}

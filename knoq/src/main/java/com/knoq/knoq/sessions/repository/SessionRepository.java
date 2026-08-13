package com.knoq.knoq.sessions.repository;

import com.knoq.knoq.sessions.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, String> {
}
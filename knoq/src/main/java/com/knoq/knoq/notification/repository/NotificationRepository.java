package com.knoq.knoq.notification.repository;

import com.knoq.knoq.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findAllBySessionIdOrderByCreatedAtDesc(String sessionId);

    void deleteAllBySessionId(String sessionId);
}

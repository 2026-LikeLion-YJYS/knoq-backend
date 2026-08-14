package com.knoq.knoq.sessions.event;

import com.knoq.knoq.sessions.entity.StorageScope;

/**
 * 쇼핑이 끝났을 때 발행되는 이벤트.
 * B팀이 이 이벤트를 받아서 자기 데이터(저장목록·니즈·상담요청·알림)를 정리하고,
 * 직원 화면에서 이 손님 카드를 지우는 데 씀.
 */
public record SessionFinishedEvent(String sessionId, StorageScope storageScope) {}
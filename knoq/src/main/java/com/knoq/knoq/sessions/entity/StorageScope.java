package com.knoq.knoq.sessions.entity;

public enum StorageScope {
    PRIVATE,             // 기본값. 계정 연결 안 함, 쇼핑 마치면 데이터 즉시 삭제
    ACCOUNT,             // 카카오 계정에 연결됨
    PENDING_KAKAO_LOGIN  // ACCOUNT를 선택했지만 아직 카카오 로그인을 안 마친 중간 상태
}
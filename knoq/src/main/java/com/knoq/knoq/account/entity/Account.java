package com.knoq.knoq.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    @Column(length = 64)
    private String id;

    // 카카오가 주는 고유 회원 번호. 계정당 하나뿐이라 unique.
    @Column(name = "kakao_user_id", nullable = false, unique = true)
    private Long kakaoUserId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Account(String id, Long kakaoUserId) {
        this.id = id;
        this.kakaoUserId = kakaoUserId;
    }

    public static Account of(String id, Long kakaoUserId) {
        return new Account(id, kakaoUserId);
    }
}
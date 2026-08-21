package com.knoq.knoq.account.repository;

import com.knoq.knoq.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByKakaoUserId(Long kakaoUserId);
}
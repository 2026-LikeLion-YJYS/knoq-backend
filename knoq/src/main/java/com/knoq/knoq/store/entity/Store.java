package com.knoq.knoq.store.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity //이 클래스는 테이블과 연결된 클래스라고 알려주는 명령어
@Table(name = "store")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_code", nullable = false, unique = true, length = 30)
    private String storeCode;

    @Column(name = "store_name", nullable = false, length = 50)
    private String storeName;

    private Store(String storeCode, String storeName) {
        this.storeCode = storeCode;
        this.storeName = storeName;
    }

    public static Store of(String storeCode, String storeName) {
        return new Store(storeCode, storeName);
    }
}
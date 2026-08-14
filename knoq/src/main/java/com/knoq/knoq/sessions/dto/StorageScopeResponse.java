package com.knoq.knoq.sessions.dto;

import com.knoq.knoq.sessions.entity.StorageScope;

public record StorageScopeResponse(
        StorageScope storageScope,
        boolean kakaoLoginRequired
) {}
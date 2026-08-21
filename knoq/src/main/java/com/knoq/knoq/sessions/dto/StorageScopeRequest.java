package com.knoq.knoq.sessions.dto;

import com.knoq.knoq.sessions.entity.StorageScope;
import jakarta.validation.constraints.NotNull;

public record StorageScopeRequest(
        @NotNull(message = "storageScope는 필수입니다.")
        StorageScope storageScope
) {}
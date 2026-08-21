package com.knoq.knoq.staff.dto;

import jakarta.validation.constraints.NotBlank;

public record StaffLoginRequest(
        @NotBlank(message = "storeCode는 필수입니다.")
        String storeCode,

        @NotBlank(message = "pin은 필수입니다.")
        String pin
) {}
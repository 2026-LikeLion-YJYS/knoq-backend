package com.knoq.knoq.staff.dto;

public record StaffLoginResponse(
        String staffToken,
        String storeName
) {}
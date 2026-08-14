package com.knoq.knoq.staff.controller;

import com.knoq.knoq.staff.dto.StaffLoginRequest;
import com.knoq.knoq.staff.dto.StaffLoginResponse;
import com.knoq.knoq.staff.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Staff", description = "직원 매장 세션 API")
@RestController
@RequestMapping("/staff/sessions")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @Operation(summary = "직원 매장 로그인")
    @PostMapping
    public ResponseEntity<StaffLoginResponse> login(
            @Valid @RequestBody StaffLoginRequest request
    ) {
        StaffLoginResponse response = staffService.login(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "POS 종료")
    @DeleteMapping
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        staffService.logout(authorizationHeader);
        return ResponseEntity.noContent().build();
    }
}
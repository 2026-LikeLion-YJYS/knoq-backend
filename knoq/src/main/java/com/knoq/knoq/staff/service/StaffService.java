package com.knoq.knoq.staff.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.staff.dto.StaffLoginRequest;
import com.knoq.knoq.staff.dto.StaffLoginResponse;
import com.knoq.knoq.staff.jwt.StaffTokenProvider;
import com.knoq.knoq.store.entity.Store;
import com.knoq.knoq.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class StaffService {

    private static final String ID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StoreRepository storeRepository;
    private final StaffTokenProvider staffTokenProvider;

    // 데모용 고정 PIN. application.yaml에 knoq.staff.pin 없으면 기본값 "1234" 사용
    @Value("${knoq.staff.pin:1234}")
    private String staffPin;

    @Transactional(readOnly = true)
    public StaffLoginResponse login(StaffLoginRequest request) {
        Store store = storeRepository.findByStoreCode(request.storeCode())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_STORE_CODE));

        if (!staffPin.equals(request.pin())) {
            throw new ApiException(ErrorCode.INVALID_PIN);
        }

        String staffSessionId = generateStaffSessionId();
        String staffToken = staffTokenProvider.generateToken(staffSessionId, store.getStoreCode());

        return new StaffLoginResponse(staffToken, store.getStoreName());
    }

    // 별도로 저장해둔 서버 상태가 없어서(stateless JWT), 토큰이 유효한지만 확인하고 끝냄
    public void logout(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);

        try {
            staffTokenProvider.parseToken(token);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return authorizationHeader.substring("Bearer ".length());
    }

    private String generateStaffSessionId() {
        StringBuilder sb = new StringBuilder("staff_sess_");
        for (int i = 0; i < 12; i++) {
            sb.append(ID_CHARS.charAt(RANDOM.nextInt(ID_CHARS.length())));
        }
        return sb.toString();
    }
}
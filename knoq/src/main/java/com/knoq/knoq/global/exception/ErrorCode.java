package com.knoq.knoq.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 공통 규칙 문서의 에러 코드 표. 앞으로 기능 만들 때마다 필요한 코드를 여기에 추가한다.
 * FR-000, FR-401(세션 조회)에 필요한 코드까지 포함.
 */
public enum ErrorCode {

    INVALID_STORE_CODE(HttpStatus.NOT_FOUND, "존재하지 않는 매장 코드입니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 정보가 없거나 유효하지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 세션입니다."),
    SESSION_EXPIRED(HttpStatus.GONE, "세션이 만료되었습니다."),
    CONSENT_REQUIRED(HttpStatus.BAD_REQUEST, "필수 약관에 모두 동의해야 합니다."),
    FORBIDDEN_WORD(HttpStatus.BAD_REQUEST, "사용할 수 없는 닉네임입니다."),
    INVALID_PIN(HttpStatus.UNAUTHORIZED, "PIN이 일치하지 않습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 제품입니다."),
    RECOGNITION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 인식 요청입니다."),
    RECOGNITION_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "이미 처리된 인식 요청입니다."),
    INVALID_RECOGNITION_CANDIDATE(HttpStatus.BAD_REQUEST, "후보 목록에 없는 제품입니다."),
    VISION_RECOGNITION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "이미지 분석에 실패했습니다."),
    SAVED_PRODUCT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "저장할 수 있는 제품 개수를 초과했습니다."),
    SAVED_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "저장된 제품을 찾을 수 없습니다."),
    NEEDS_ANALYSIS_NOT_ENOUGH_SAVED_PRODUCTS(HttpStatus.BAD_REQUEST, "저장된 제품이 2개 이상이어야 니즈 분석이 가능합니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
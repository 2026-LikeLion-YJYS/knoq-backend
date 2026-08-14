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
    FORBIDDEN_WORD(HttpStatus.BAD_REQUEST, "사용할 수 없는 닉네임입니다.");

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
package com.knoq.knoq.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 공통 규칙 문서의 에러 코드 표와 1:1로 맞춘다.
 * 새 에러가 필요하면 여기에만 추가하면 GlobalExceptionHandler가 알아서 처리한다.
 */
public enum ErrorCode {

    INVALID_STORE_CODE(HttpStatus.NOT_FOUND, "존재하지 않는 매장 코드입니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 정보가 없거나 유효하지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 세션입니다."),
    SESSION_EXPIRED(HttpStatus.GONE, "세션이 만료되었습니다.");

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
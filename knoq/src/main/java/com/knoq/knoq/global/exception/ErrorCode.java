package com.knoq.knoq.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 공통 규칙 문서의 에러 코드 표. 앞으로 기능 만들 때마다 필요한 코드를 여기에 추가한다.
 * 지금은 FR-000에 필요한 2개만.
 */
public enum ErrorCode {

    INVALID_STORE_CODE(HttpStatus.NOT_FOUND, "존재하지 않는 매장 코드입니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다.");

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
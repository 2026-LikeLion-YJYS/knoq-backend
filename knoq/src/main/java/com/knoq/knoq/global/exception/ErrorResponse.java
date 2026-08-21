package com.knoq.knoq.global.exception;

public record ErrorResponse(ErrorBody error) {

    public record ErrorBody(String code, String message) {
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(new ErrorBody(errorCode.name(), errorCode.getMessage()));
    }
}
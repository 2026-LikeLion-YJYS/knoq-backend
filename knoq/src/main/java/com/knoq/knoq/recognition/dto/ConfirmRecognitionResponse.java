package com.knoq.knoq.recognition.dto;

public record ConfirmRecognitionResponse(
        String productId,
        boolean confirmed
) {}
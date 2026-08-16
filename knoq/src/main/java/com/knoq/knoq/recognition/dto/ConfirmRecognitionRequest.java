package com.knoq.knoq.recognition.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmRecognitionRequest(
        @NotBlank
        String productId,

        @NotNull
        Boolean confirmed
) {}
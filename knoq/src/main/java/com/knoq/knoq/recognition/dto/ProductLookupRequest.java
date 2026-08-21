package com.knoq.knoq.recognition.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductLookupRequest(
        @NotBlank
        String productCode
) {}
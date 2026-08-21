package com.knoq.knoq.sessions.dto;

import jakarta.validation.constraints.NotNull;

public record ConsentRequest(
        @NotNull(message = "termsOfService는 필수입니다.")
        Boolean termsOfService,

        @NotNull(message = "privacyPolicy는 필수입니다.")
        Boolean privacyPolicy,

        @NotNull(message = "over14는 필수입니다.")
        Boolean over14,

        @NotNull(message = "marketingOptIn은 필수입니다.")
        Boolean marketingOptIn
) {}
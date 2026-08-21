package com.knoq.knoq.sessions.dto;

import com.knoq.knoq.sessions.entity.LifestyleTag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record LifestyleTagsRequest(
        @NotNull(message = "tags는 필수입니다.")
        @Size(min = 1, max = 3, message = "태그는 1~3개 선택해야 합니다.")
        List<LifestyleTag> tags
) {}
package com.knoq.knoq.sessions.dto;

import com.knoq.knoq.sessions.entity.LifestyleTag;

import java.util.List;

public record LifestyleTagsResponse(
        List<LifestyleTag> tags
) {}
package com.blocksy.server.modules.admin.dto;

public record ContentTrendPointResponse(
        String day,
        Long postCount,
        Long commentCount
) {
}

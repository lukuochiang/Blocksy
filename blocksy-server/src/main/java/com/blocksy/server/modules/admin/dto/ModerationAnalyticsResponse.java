package com.blocksy.server.modules.admin.dto;

public record ModerationAnalyticsResponse(
        Long totalReports,
        Long pendingReports,
        Long handledReports,
        Double pendingRate,
        Double handledRate
) {
}

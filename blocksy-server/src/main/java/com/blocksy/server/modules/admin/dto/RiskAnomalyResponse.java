package com.blocksy.server.modules.admin.dto;

import java.time.LocalDateTime;

public record RiskAnomalyResponse(
        Long id,
        Long userId,
        String anomalyType,
        String level,
        String details,
        String processStatus,
        String handleNote,
        Long assigneeUserId,
        LocalDateTime processedAt,
        LocalDateTime createdAt
) {
}

package com.blocksy.server.modules.admin.dto;

import java.time.LocalDateTime;

public record RiskAppealResponse(
        Long id,
        Long userId,
        Long punishLogId,
        String appealReason,
        String appealContent,
        String processStatus,
        String resultNote,
        Long assigneeUserId,
        LocalDateTime processedAt,
        LocalDateTime createdAt
) {
}

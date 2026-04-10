package com.blocksy.server.modules.admin.dto;

import java.time.LocalDateTime;

public record AdminOperationLogResponse(
        Long id,
        String module,
        String action,
        Long operatorUserId,
        String targetType,
        Long targetId,
        String details,
        LocalDateTime createdAt
) {
}

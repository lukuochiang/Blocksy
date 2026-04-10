package com.blocksy.server.modules.admin.dto;

import java.time.LocalDateTime;

public record PushRecordResponse(
        Long id,
        Long taskId,
        Long userId,
        String channel,
        String sendStatus,
        Boolean readStatus,
        LocalDateTime deliveredAt,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}

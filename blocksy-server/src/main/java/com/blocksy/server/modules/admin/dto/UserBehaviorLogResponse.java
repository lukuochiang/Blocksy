package com.blocksy.server.modules.admin.dto;

import java.time.LocalDateTime;

public record UserBehaviorLogResponse(
        Long id,
        Long userId,
        String behaviorType,
        String resourceType,
        Long resourceId,
        String ip,
        String device,
        LocalDateTime createdAt
) {
}

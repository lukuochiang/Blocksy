package com.blocksy.server.modules.admin.dto;

import java.time.LocalDateTime;

public record PermissionOpLogResponse(
        Long id,
        String roleCode,
        Long operatorUserId,
        String action,
        String details,
        LocalDateTime createdAt
) {
}

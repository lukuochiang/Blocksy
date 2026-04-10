package com.blocksy.server.modules.admin.dto;

import java.time.LocalDateTime;

public record DataPermissionItemResponse(
        Long id,
        String roleCode,
        String dataScope,
        String dataValue,
        Boolean enabled,
        LocalDateTime updatedAt
) {
}

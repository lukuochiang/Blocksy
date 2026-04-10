package com.blocksy.server.modules.admin.dto;

import java.time.LocalDateTime;

public record MenuPermissionItemResponse(
        Long id,
        String roleCode,
        String menuKey,
        String menuName,
        String menuPath,
        Boolean enabled,
        LocalDateTime updatedAt
) {
}

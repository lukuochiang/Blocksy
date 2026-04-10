package com.blocksy.server.modules.admin.dto;

import java.time.LocalDateTime;

public record ContentCategoryResponse(
        Long id,
        String module,
        String code,
        String name,
        Integer sortNo,
        Boolean enabled,
        LocalDateTime updatedAt
) {
}

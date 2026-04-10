package com.blocksy.server.modules.admin.dto;

import java.time.LocalDateTime;

public record NotificationTemplateResponse(
        Long id,
        String module,
        String triggerCode,
        String titleTemplate,
        String contentTemplate,
        Boolean enabled,
        LocalDateTime updatedAt
) {
}

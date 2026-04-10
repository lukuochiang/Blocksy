package com.blocksy.server.modules.admin.dto;

import java.time.LocalDateTime;

public record PlatformSettingResponse(
        Long id,
        String settingGroup,
        String settingKey,
        String settingValue,
        String description,
        LocalDateTime updatedAt
) {
}

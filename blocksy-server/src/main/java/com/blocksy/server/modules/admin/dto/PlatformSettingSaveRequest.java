package com.blocksy.server.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlatformSettingSaveRequest(
        @NotBlank(message = "settingGroup 不能为空")
        @Size(max = 64, message = "settingGroup 不能超过 64 字符")
        String settingGroup,
        @NotBlank(message = "settingKey 不能为空")
        @Size(max = 128, message = "settingKey 不能超过 128 字符")
        String settingKey,
        @Size(max = 2000, message = "settingValue 不能超过 2000 字符")
        String settingValue,
        @Size(max = 255, message = "description 不能超过 255 字符")
        String description
) {
}

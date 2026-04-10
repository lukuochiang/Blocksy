package com.blocksy.server.modules.admin.dto;

import jakarta.validation.constraints.NotNull;

public record ContentCategoryToggleRequest(
        @NotNull(message = "enabled 不能为空")
        Boolean enabled
) {
}

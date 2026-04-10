package com.blocksy.server.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record AdminUserBanRequest(
        @Schema(description = "封禁原因", example = "恶意刷屏")
        @Size(max = 500, message = "reason 长度不能超过 500")
        String reason,
        @Schema(description = "封禁时长（小时），为空表示永久封禁", example = "24")
        @Min(value = 1, message = "durationHours 不能小于 1")
        @Max(value = 24 * 365, message = "durationHours 不能超过 8760")
        Integer durationHours
) {
}

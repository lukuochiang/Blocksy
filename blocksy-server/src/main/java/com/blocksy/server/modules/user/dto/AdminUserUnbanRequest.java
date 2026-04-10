package com.blocksy.server.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record AdminUserUnbanRequest(
        @Schema(description = "解封原因/备注", example = "申诉通过")
        @Size(max = 500, message = "reason 长度不能超过 500")
        String reason
) {
}

package com.blocksy.server.modules.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record EventSignupResponse(
        @Schema(description = "报名记录 ID", example = "7001")
        Long id,
        @Schema(description = "活动 ID", example = "2001")
        Long eventId,
        @Schema(description = "用户 ID", example = "1")
        Long userId,
        @Schema(description = "报名备注")
        String remark,
        @Schema(description = "报名时间")
        LocalDateTime createdAt
) {
}

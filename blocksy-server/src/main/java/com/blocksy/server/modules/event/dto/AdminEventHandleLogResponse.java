package com.blocksy.server.modules.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AdminEventHandleLogResponse(
        @Schema(description = "日志 ID", example = "1")
        Long id,
        @Schema(description = "活动 ID", example = "2001")
        Long eventId,
        @Schema(description = "操作人用户 ID", example = "1")
        Long operatorUserId,
        @Schema(description = "动作", example = "OFFLINE")
        String action,
        @Schema(description = "备注")
        String note,
        @Schema(description = "操作时间")
        LocalDateTime createdAt
) {
}

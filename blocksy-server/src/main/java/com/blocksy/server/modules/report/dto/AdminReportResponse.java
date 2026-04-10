package com.blocksy.server.modules.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AdminReportResponse(
        @Schema(description = "举报 ID")
        Long id,
        @Schema(description = "举报人用户 ID")
        Long reporterUserId,
        @Schema(description = "目标类型")
        String targetType,
        @Schema(description = "目标 ID")
        Long targetId,
        @Schema(description = "举报原因")
        String reason,
        @Schema(description = "处理状态：PENDING/RESOLVED/REJECTED")
        String processStatus,
        @Schema(description = "处理人 ID")
        Long handlerUserId,
        @Schema(description = "处理备注")
        String handlerNote,
        @Schema(description = "处理时间")
        LocalDateTime handledAt,
        @Schema(description = "创建时间")
        LocalDateTime createdAt
) {
}

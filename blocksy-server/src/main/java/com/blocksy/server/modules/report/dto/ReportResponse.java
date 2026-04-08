package com.blocksy.server.modules.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ReportResponse(
        @Schema(description = "举报 ID", example = "8001")
        Long id,
        @Schema(description = "举报目标类型")
        String targetType,
        @Schema(description = "举报目标 ID")
        Long targetId,
        @Schema(description = "举报原因")
        String reason,
        @Schema(description = "处理状态", example = "PENDING")
        String processStatus,
        @Schema(description = "创建时间")
        LocalDateTime createdAt
) {
}

package com.blocksy.server.modules.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AdminReportHandleLogResponse(
        @Schema(description = "日志 ID")
        Long id,
        @Schema(description = "举报 ID")
        Long reportId,
        @Schema(description = "操作人 ID")
        Long operatorUserId,
        @Schema(description = "操作动作")
        String action,
        @Schema(description = "处理备注")
        String note,
        @Schema(description = "是否封禁被举报用户")
        Boolean banTargetUser,
        @Schema(description = "操作时间")
        LocalDateTime createdAt
) {
}

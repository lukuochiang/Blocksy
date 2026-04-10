package com.blocksy.server.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record OperationNoteTemplateLogResponse(
        @Schema(description = "日志ID")
        Long id,
        @Schema(description = "模板ID")
        Long templateId,
        @Schema(description = "操作人用户ID")
        Long operatorUserId,
        @Schema(description = "动作 CREATE/UPDATE/ENABLE/DISABLE")
        String action,
        @Schema(description = "备注")
        String note,
        @Schema(description = "创建时间")
        LocalDateTime createdAt
) {
}

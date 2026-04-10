package com.blocksy.server.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OperationNoteTemplateSaveRequest(
        @Schema(description = "模块 EVENT/LISTING/REPORT", example = "EVENT")
        @NotBlank(message = "module 不能为空")
        String module,
        @Schema(description = "动作 OFFLINE/RESTORE/DELETE", example = "OFFLINE")
        @NotBlank(message = "action 不能为空")
        String action,
        @Schema(description = "模板内容")
        @NotBlank(message = "content 不能为空")
        @Size(max = 255, message = "content 长度不能超过 255")
        String content,
        @Schema(description = "排序")
        Integer sortNo,
        @Schema(description = "状态 1启用 0禁用")
        Integer status
) {
}

package com.blocksy.server.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record OperationNoteTemplateResponse(
        @Schema(description = "模板ID")
        Long id,
        @Schema(description = "模块 EVENT/LISTING/REPORT")
        String module,
        @Schema(description = "动作 OFFLINE/RESTORE/DELETE 等")
        String action,
        @Schema(description = "模板内容")
        String content,
        @Schema(description = "排序")
        Integer sortNo,
        @Schema(description = "状态 1启用 0禁用")
        Integer status
) {
}

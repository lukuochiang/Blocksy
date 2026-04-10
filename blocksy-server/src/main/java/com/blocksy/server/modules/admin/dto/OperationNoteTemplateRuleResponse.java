package com.blocksy.server.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record OperationNoteTemplateRuleResponse(
        @Schema(description = "模块", example = "EVENT")
        String module,
        @Schema(description = "可选动作列表")
        List<String> actions
) {
}

package com.blocksy.server.modules.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminReportHandleRequest(
        @Schema(description = "处理动作：RESOLVED/REJECTED", example = "RESOLVED")
        @NotBlank(message = "action 不能为空")
        String action,
        @Schema(description = "处理备注")
        @Size(max = 500, message = "note 长度不能超过 500")
        String note,
        @Schema(description = "是否封禁被举报用户（仅 targetType=POST 时有效）", example = "false")
        Boolean banTargetUser
) {
}

package com.blocksy.server.modules.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminReportBatchRetryRequest(
        @Schema(description = "失败举报 ID 列表", example = "[201,202]")
        @NotEmpty(message = "failedReportIds 不能为空")
        List<Long> failedReportIds,
        @Schema(description = "处理动作：RESOLVED/REJECTED", example = "RESOLVED")
        @NotBlank(message = "action 不能为空")
        String action,
        @Schema(description = "重试备注")
        @Size(max = 500, message = "note 长度不能超过 500")
        String note,
        @Schema(description = "是否封禁被举报用户（仅 targetType=POST 时有效）", example = "false")
        Boolean banTargetUser
) {
}

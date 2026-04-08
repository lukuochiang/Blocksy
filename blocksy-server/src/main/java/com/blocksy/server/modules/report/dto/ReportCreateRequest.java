package com.blocksy.server.modules.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportCreateRequest(
        @Schema(description = "举报目标类型：POST/LISTING/EVENT", example = "POST")
        @NotBlank(message = "targetType 不能为空")
        String targetType,
        @Schema(description = "举报目标 ID", example = "1001")
        @NotNull(message = "targetId 不能为空")
        Long targetId,
        @Schema(description = "举报原因", example = "垃圾信息")
        @NotBlank(message = "reason 不能为空")
        String reason,
        @Schema(description = "补充说明")
        String description
) {
}

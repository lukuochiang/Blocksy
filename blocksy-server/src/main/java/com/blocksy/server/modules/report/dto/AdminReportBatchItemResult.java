package com.blocksy.server.modules.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminReportBatchItemResult(
        @Schema(description = "举报 ID")
        Long reportId,
        @Schema(description = "结果说明")
        String message
) {
}

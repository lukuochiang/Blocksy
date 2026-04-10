package com.blocksy.server.modules.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AdminReportBatchHandleResponse(
        @Schema(description = "请求总数")
        Integer totalCount,
        @Schema(description = "成功处理数量")
        Integer successCount,
        @Schema(description = "成功处理的举报 ID")
        List<Long> successIds,
        @Schema(description = "跳过处理的举报 ID（通常为非 PENDING）")
        List<Long> skippedIds,
        @Schema(description = "失败列表")
        List<AdminReportBatchItemResult> failedItems
) {
}

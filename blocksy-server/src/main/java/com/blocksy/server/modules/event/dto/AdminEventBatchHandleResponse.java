package com.blocksy.server.modules.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AdminEventBatchHandleResponse(
        @Schema(description = "请求总数")
        Integer totalCount,
        @Schema(description = "成功处理数量")
        Integer successCount,
        @Schema(description = "成功处理的活动 ID")
        List<Long> successIds,
        @Schema(description = "跳过处理的活动 ID（状态无需变更）")
        List<Long> skippedIds,
        @Schema(description = "失败列表")
        List<AdminEventBatchItemResult> failedItems
) {
}

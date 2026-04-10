package com.blocksy.server.modules.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "后台分类信息批量处理响应")
public record AdminListingBatchHandleResponse(
        @Schema(description = "请求总数", example = "3")
        int total,
        @Schema(description = "成功数", example = "2")
        int successCount,
        @Schema(description = "失败数", example = "1")
        int failCount,
        @Schema(description = "逐条结果")
        List<AdminListingBatchItemResult> items
) {
}

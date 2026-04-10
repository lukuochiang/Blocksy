package com.blocksy.server.modules.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "后台分类信息批量处理单条结果")
public record AdminListingBatchItemResult(
        @Schema(description = "分类信息 ID", example = "1001")
        Long listingId,
        @Schema(description = "是否成功", example = "true")
        boolean success,
        @Schema(description = "结果信息", example = "ok")
        String message
) {
}

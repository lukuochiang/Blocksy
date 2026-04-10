package com.blocksy.server.modules.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "分类信息分类维度统计")
public record ListingCategoryStatResponse(
        @Schema(description = "分类编码", example = "SECOND_HAND")
        String category,
        @Schema(description = "总数", example = "16")
        long totalCount,
        @Schema(description = "待审核数(status=2)", example = "4")
        long pendingCount,
        @Schema(description = "已上架数(status=1)", example = "10")
        long onlineCount,
        @Schema(description = "已下架/驳回数(status=0)", example = "2")
        long offlineCount
) {
}

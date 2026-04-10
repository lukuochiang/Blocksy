package com.blocksy.server.modules.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AdminListingResponse(
        @Schema(description = "分类信息 ID", example = "1001")
        Long id,
        @Schema(description = "发布用户 ID", example = "1")
        Long userId,
        @Schema(description = "社区 ID", example = "1")
        Long communityId,
        @Schema(description = "分类", example = "SECOND_HAND")
        String category,
        @Schema(description = "标题")
        String title,
        @Schema(description = "内容")
        String content,
        @Schema(description = "状态，2=待审核，1=正常，0=下架/驳回")
        Integer status,
        @Schema(description = "创建时间")
        LocalDateTime createdAt,
        @Schema(description = "更新时间")
        LocalDateTime updatedAt
) {
}

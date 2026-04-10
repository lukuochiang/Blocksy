package com.blocksy.server.modules.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ListingResponse(
        @Schema(description = "分类信息 ID", example = "3001")
        Long id,
        @Schema(description = "所属社区 ID", example = "1")
        Long communityId,
        @Schema(description = "发布用户 ID", example = "1")
        Long userId,
        @Schema(description = "分类")
        String category,
        @Schema(description = "信息标题")
        String title,
        @Schema(description = "信息内容")
        String content,
        @Schema(description = "价格")
        Double price,
        @Schema(description = "联系方式")
        String contact,
        @Schema(description = "封面 objectKey")
        String coverObjectKey,
        @Schema(description = "封面 URL")
        String coverUrl,
        @Schema(description = "状态，2=待审核，1=正常，0=下架/驳回")
        Integer status,
        @Schema(description = "创建时间")
        LocalDateTime createdAt
) {
}

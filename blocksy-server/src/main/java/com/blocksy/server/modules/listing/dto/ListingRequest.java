package com.blocksy.server.modules.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ListingRequest(
        @Schema(description = "所属社区 ID", example = "1")
        @NotNull(message = "communityId 不能为空")
        Long communityId,
        @Schema(description = "分类：SECOND_HAND/LOST_FOUND/HELP_WANTED", example = "SECOND_HAND")
        @NotBlank(message = "category 不能为空")
        String category,
        @Schema(description = "信息标题", example = "二手咖啡机")
        @NotBlank(message = "标题不能为空") String title,
        @Schema(description = "信息内容", example = "九成新，家用款")
        @NotBlank(message = "content 不能为空")
        String content,
        @Schema(description = "价格", example = "299.00")
        Double price,
        @Schema(description = "联系方式", example = "13800000000")
        String contact,
        @Schema(description = "封面 objectKey", example = "listing-xxx.png")
        String coverObjectKey,
        @Schema(description = "封面 URL")
        String coverUrl
) {
}

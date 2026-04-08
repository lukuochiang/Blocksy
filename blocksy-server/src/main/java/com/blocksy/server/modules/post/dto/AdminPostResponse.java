package com.blocksy.server.modules.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AdminPostResponse(
        @Schema(description = "帖子 ID", example = "101")
        Long id,
        @Schema(description = "作者用户 ID", example = "1")
        Long userId,
        @Schema(description = "所属社区 ID", example = "1")
        Long communityId,
        @Schema(description = "帖子内容")
        String content,
        @Schema(description = "状态：1 正常，0 下架", example = "1")
        Integer status,
        @Schema(description = "评论数", example = "2")
        Integer commentCount,
        @Schema(description = "点赞数", example = "0")
        Integer likeCount,
        @Schema(description = "创建时间")
        LocalDateTime createdAt,
        @Schema(description = "更新时间")
        LocalDateTime updatedAt
) {
}

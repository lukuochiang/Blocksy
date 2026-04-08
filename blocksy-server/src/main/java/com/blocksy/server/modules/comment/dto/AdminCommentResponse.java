package com.blocksy.server.modules.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AdminCommentResponse(
        @Schema(description = "评论 ID", example = "1")
        Long id,
        @Schema(description = "帖子 ID", example = "1")
        Long postId,
        @Schema(description = "用户 ID", example = "1")
        Long userId,
        @Schema(description = "评论内容")
        String content,
        @Schema(description = "状态：1 正常，0 删除")
        Integer status,
        @Schema(description = "创建时间")
        LocalDateTime createdAt,
        @Schema(description = "更新时间")
        LocalDateTime updatedAt
) {
}

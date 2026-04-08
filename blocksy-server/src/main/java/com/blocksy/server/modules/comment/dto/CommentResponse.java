package com.blocksy.server.modules.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CommentResponse(
        @Schema(description = "评论 ID", example = "5001")
        Long id,
        @Schema(description = "帖子 ID", example = "1001")
        Long postId,
        @Schema(description = "评论用户 ID", example = "1")
        Long userId,
        @Schema(description = "评论内容")
        String content,
        @Schema(description = "评论时间")
        LocalDateTime createdAt
) {
}

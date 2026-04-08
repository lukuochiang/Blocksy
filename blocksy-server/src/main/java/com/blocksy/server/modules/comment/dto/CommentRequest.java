package com.blocksy.server.modules.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentRequest(
        @Schema(description = "帖子 ID", example = "1001")
        @NotNull(message = "postId 不能为空") Long postId,
        @Schema(description = "回复评论 ID（可空）", example = "5001")
        Long replyToCommentId,
        @Schema(description = "评论内容", example = "支持，已点赞")
        @NotBlank(message = "评论内容不能为空") String content
) {
}

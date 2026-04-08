package com.blocksy.server.modules.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        @Schema(description = "帖子 ID", example = "1001")
        Long id,
        @Schema(description = "所属社区 ID", example = "1")
        Long communityId,
        @Schema(description = "帖子正文")
        String content,
        @Schema(description = "作者用户 ID", example = "1")
        Long authorId,
        @Schema(description = "评论数", example = "0")
        Integer commentCount,
        @Schema(description = "点赞数", example = "0")
        Integer likeCount,
        @Schema(description = "发布时间")
        LocalDateTime createdAt,
        @Schema(description = "媒体列表")
        List<PostMediaItem> media
) {
}

package com.blocksy.server.modules.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PostRequest(
        @Schema(description = "所属社区 ID", example = "1")
        @NotNull(message = "communityId 不能为空")
        Long communityId,
        @Schema(description = "帖子正文", example = "周末有人一起骑行吗？")
        @NotBlank(message = "帖子内容不能为空") String content,
        @Schema(description = "帖子媒体列表")
        List<@Valid PostMediaItem> media
) {
}

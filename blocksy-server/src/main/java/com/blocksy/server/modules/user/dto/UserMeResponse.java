package com.blocksy.server.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserMeResponse(
        @Schema(description = "用户 ID", example = "1")
        Long id,
        @Schema(description = "用户名", example = "demo")
        String username,
        @Schema(description = "昵称", example = "Norvo Demo")
        String nickname,
        @Schema(description = "头像 URL")
        String avatarUrl,
        @Schema(description = "状态：1 正常，0 封禁")
        Integer status,
        @Schema(description = "默认社区 ID", example = "1")
        Long defaultCommunityId
) {
}

package com.blocksy.server.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record SelectCommunityRequest(
        @Schema(description = "要切换的社区 ID", example = "1")
        @NotNull(message = "communityId 不能为空")
        Long communityId
) {
}

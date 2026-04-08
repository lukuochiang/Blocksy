package com.blocksy.server.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserCommunityItemResponse(
        @Schema(description = "社区 ID", example = "1")
        Long communityId,
        @Schema(description = "社区编码", example = "DEFAULT-COMMUNITY")
        String communityCode,
        @Schema(description = "社区名称", example = "Norvo 默认社区")
        String communityName,
        @Schema(description = "是否当前默认社区", example = "true")
        Boolean isDefault
) {
}

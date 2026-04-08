package com.blocksy.server.modules.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CommunityResponse(
        @Schema(description = "社区 ID", example = "1")
        Long id,
        @Schema(description = "社区编码", example = "SH-PD-001")
        String code,
        @Schema(description = "社区名称", example = "绿苑社区")
        String name,
        @Schema(description = "地址")
        String address,
        @Schema(description = "描述")
        String description
) {
}

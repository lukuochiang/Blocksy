package com.blocksy.server.modules.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AdminCommunityCreateRequest(
        @Schema(description = "社区编码", example = "SH-PD-001")
        @NotBlank(message = "code 不能为空")
        String code,
        @Schema(description = "社区名称", example = "花木街道第一社区")
        @NotBlank(message = "name 不能为空")
        String name,
        @Schema(description = "地址")
        String address,
        @Schema(description = "描述")
        String description
) {
}

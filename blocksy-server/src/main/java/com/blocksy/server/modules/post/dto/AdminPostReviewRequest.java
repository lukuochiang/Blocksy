package com.blocksy.server.modules.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AdminPostReviewRequest(
        @Schema(description = "审核动作：APPROVE(通过), REJECT(下架)", example = "REJECT")
        @NotBlank(message = "action 不能为空")
        @Pattern(regexp = "APPROVE|REJECT", message = "action 仅支持 APPROVE 或 REJECT")
        String action
) {
}

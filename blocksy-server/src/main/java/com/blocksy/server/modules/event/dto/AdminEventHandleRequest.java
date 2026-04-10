package com.blocksy.server.modules.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AdminEventHandleRequest(
        @Schema(description = "处理动作：OFFLINE/RESTORE/DELETE", example = "OFFLINE")
        @NotBlank(message = "action 不能为空")
        String action,
        @Schema(description = "处理备注", example = "违规活动内容，已下架")
        String note
) {
}

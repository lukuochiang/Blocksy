package com.blocksy.server.modules.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminEventBatchHandleRequest(
        @Schema(description = "活动 ID 列表", example = "[1,2,3]")
        @NotEmpty(message = "eventIds 不能为空")
        List<Long> eventIds,
        @Schema(description = "处理动作：OFFLINE/RESTORE/DELETE", example = "OFFLINE")
        @NotBlank(message = "action 不能为空")
        String action,
        @Schema(description = "处理备注")
        @Size(max = 500, message = "note 长度不能超过 500")
        String note
) {
}

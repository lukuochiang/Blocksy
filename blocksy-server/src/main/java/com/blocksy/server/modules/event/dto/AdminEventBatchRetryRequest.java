package com.blocksy.server.modules.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminEventBatchRetryRequest(
        @Schema(description = "失败活动 ID 列表", example = "[101,102]")
        @NotEmpty(message = "failedEventIds 不能为空")
        List<Long> failedEventIds,
        @Schema(description = "处理动作：OFFLINE/RESTORE/DELETE", example = "OFFLINE")
        @NotBlank(message = "action 不能为空")
        String action,
        @Schema(description = "重试备注")
        @Size(max = 500, message = "note 长度不能超过 500")
        String note
) {
}

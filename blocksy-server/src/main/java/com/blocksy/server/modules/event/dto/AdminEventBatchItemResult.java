package com.blocksy.server.modules.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminEventBatchItemResult(
        @Schema(description = "活动 ID")
        Long eventId,
        @Schema(description = "结果说明")
        String message
) {
}

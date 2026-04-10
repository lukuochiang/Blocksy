package com.blocksy.server.modules.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record NotificationBatchReadRequest(
        @Schema(description = "通知 ID 列表", example = "[1,2,3]")
        @NotEmpty(message = "notificationIds 不能为空")
        List<Long> notificationIds
) {
}

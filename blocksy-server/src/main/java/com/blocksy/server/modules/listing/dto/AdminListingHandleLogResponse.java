package com.blocksy.server.modules.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AdminListingHandleLogResponse(
        @Schema(description = "日志 ID", example = "1")
        Long id,
        @Schema(description = "分类信息 ID", example = "1001")
        Long listingId,
        @Schema(description = "操作人用户 ID", example = "1")
        Long operatorUserId,
        @Schema(description = "动作", example = "OFFLINE")
        String action,
        @Schema(description = "备注")
        String note,
        @Schema(description = "操作时间")
        LocalDateTime createdAt
) {
}

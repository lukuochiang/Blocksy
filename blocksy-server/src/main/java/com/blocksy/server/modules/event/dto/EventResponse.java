package com.blocksy.server.modules.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record EventResponse(
        @Schema(description = "活动 ID", example = "2001")
        Long id,
        @Schema(description = "所属社区 ID", example = "1")
        Long communityId,
        @Schema(description = "发布用户 ID", example = "1")
        Long userId,
        @Schema(description = "活动标题", example = "周末亲子手工活动")
        String title,
        @Schema(description = "活动内容")
        String content,
        @Schema(description = "活动地点")
        String location,
        @Schema(description = "封面 objectKey")
        String coverObjectKey,
        @Schema(description = "封面 URL")
        String coverUrl,
        @Schema(description = "活动开始时间")
        LocalDateTime startTime,
        @Schema(description = "活动结束时间")
        LocalDateTime endTime,
        @Schema(description = "报名上限", example = "50")
        Integer signupLimit,
        @Schema(description = "当前报名人数", example = "10")
        Integer signupCount,
        @Schema(description = "状态 1=正常 0=下架", example = "1")
        Integer status
) {
}

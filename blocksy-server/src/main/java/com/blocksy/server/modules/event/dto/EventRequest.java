package com.blocksy.server.modules.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EventRequest(
        @Schema(description = "所属社区 ID", example = "1")
        @NotNull(message = "communityId 不能为空")
        Long communityId,
        @Schema(description = "活动标题", example = "周末亲子手工活动")
        @NotBlank(message = "活动标题不能为空") String title,
        @Schema(description = "活动内容", example = "周六下午社区活动室集合")
        @NotBlank(message = "content 不能为空")
        String content,
        @Schema(description = "活动地点", example = "社区活动室")
        String location,
        @Schema(description = "封面 objectKey", example = "event-001.png")
        String coverObjectKey,
        @Schema(description = "封面 URL")
        String coverUrl,
        @Schema(description = "活动开始时间", example = "2026-04-12T14:00:00")
        @NotNull(message = "开始时间不能为空") LocalDateTime startTime,
        @Schema(description = "活动结束时间", example = "2026-04-12T18:00:00")
        LocalDateTime endTime,
        @Schema(description = "报名人数上限", example = "50")
        Integer signupLimit
) {
}

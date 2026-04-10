package com.blocksy.server.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "分析口径配置")
public record AnalyticsConfigResponse(
        @Schema(description = "默认时间窗口天数")
        Integer defaultWindowDays,
        @Schema(description = "最大时间窗口天数")
        Integer maxWindowDays,
        @Schema(description = "前端可选时间窗口（天）")
        List<Integer> allowedWindowDays,
        @Schema(description = "指标定义列表")
        List<AnalyticsMetricDefinitionResponse> metricDefinitions,
        @Schema(description = "配置更新时间")
        LocalDateTime updatedAt
) {
}

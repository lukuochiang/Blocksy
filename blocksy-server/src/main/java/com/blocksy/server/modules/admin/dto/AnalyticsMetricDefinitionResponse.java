package com.blocksy.server.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "分析指标定义")
public record AnalyticsMetricDefinitionResponse(
        @Schema(description = "指标编码")
        String metricCode,
        @Schema(description = "指标名称")
        String metricName,
        @Schema(description = "口径说明")
        String definition,
        @Schema(description = "计算公式")
        String formula,
        @Schema(description = "数据来源")
        String dataSource
) {
}

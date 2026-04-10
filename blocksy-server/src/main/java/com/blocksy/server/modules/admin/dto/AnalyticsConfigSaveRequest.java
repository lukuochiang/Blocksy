package com.blocksy.server.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "分析口径配置保存请求")
public record AnalyticsConfigSaveRequest(
        @NotNull(message = "defaultWindowDays 不能为空")
        @Min(value = 1, message = "defaultWindowDays 必须大于 0")
        @Schema(description = "默认时间窗口天数", example = "7")
        Integer defaultWindowDays,

        @NotNull(message = "maxWindowDays 不能为空")
        @Min(value = 1, message = "maxWindowDays 必须大于 0")
        @Schema(description = "最大时间窗口天数", example = "90")
        Integer maxWindowDays,

        @NotEmpty(message = "allowedWindowDays 不能为空")
        @Schema(description = "允许前端选择的时间窗口天数列表", example = "[7,14,30,90]")
        List<Integer> allowedWindowDays
) {
}

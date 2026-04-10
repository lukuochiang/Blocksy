package com.blocksy.server.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record GovernanceStatsResponse(
        @Schema(description = "待处理举报数")
        Long pendingReports,
        @Schema(description = "今日已处理举报数")
        Long handledReportsToday,
        @Schema(description = "近7天平均处理耗时(小时)")
        Double avgHandleHours7d,
        @Schema(description = "重复违规用户数(处罚记录>=2)")
        Long repeatPunishedUsers
) {
}

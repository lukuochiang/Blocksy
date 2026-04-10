package com.blocksy.server.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.modules.admin.dto.GovernanceStatsResponse;
import com.blocksy.server.modules.admin.service.GovernanceStatsService;
import com.blocksy.server.modules.report.entity.ReportEntity;
import com.blocksy.server.modules.report.mapper.ReportMapper;
import com.blocksy.server.modules.user.entity.UserPunishLogEntity;
import com.blocksy.server.modules.user.mapper.UserPunishLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GovernanceStatsServiceImpl implements GovernanceStatsService {

    private final ReportMapper reportMapper;
    private final UserPunishLogMapper userPunishLogMapper;

    public GovernanceStatsServiceImpl(ReportMapper reportMapper, UserPunishLogMapper userPunishLogMapper) {
        this.reportMapper = reportMapper;
        this.userPunishLogMapper = userPunishLogMapper;
    }

    @Override
    public GovernanceStatsResponse getOverview() {
        Long pendingReports = reportMapper.selectCount(
                new LambdaQueryWrapper<ReportEntity>()
                        .eq(ReportEntity::getStatus, 1)
                        .eq(ReportEntity::getProcessStatus, "PENDING")
        );

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        Long handledToday = reportMapper.selectCount(
                new LambdaQueryWrapper<ReportEntity>()
                        .eq(ReportEntity::getStatus, 1)
                        .in(ReportEntity::getProcessStatus, List.of("RESOLVED", "REJECTED"))
                        .ge(ReportEntity::getHandledAt, startOfToday)
        );

        LocalDateTime startOf7d = LocalDateTime.now().minusDays(7);
        List<ReportEntity> recentHandled = reportMapper.selectList(
                new LambdaQueryWrapper<ReportEntity>()
                        .eq(ReportEntity::getStatus, 1)
                        .in(ReportEntity::getProcessStatus, List.of("RESOLVED", "REJECTED"))
                        .isNotNull(ReportEntity::getHandledAt)
                        .ge(ReportEntity::getHandledAt, startOf7d)
                        .select(ReportEntity::getCreatedAt, ReportEntity::getHandledAt)
                        .last("LIMIT 5000")
        );
        double avgHandleHours7d = 0;
        if (!recentHandled.isEmpty()) {
            double totalHours = 0;
            int count = 0;
            for (ReportEntity row : recentHandled) {
                if (row.getCreatedAt() != null && row.getHandledAt() != null) {
                    totalHours += java.time.Duration.between(row.getCreatedAt(), row.getHandledAt()).toMinutes() / 60.0;
                    count++;
                }
            }
            avgHandleHours7d = count == 0 ? 0 : totalHours / count;
        }

        List<UserPunishLogEntity> punishLogs = userPunishLogMapper.selectList(
                new LambdaQueryWrapper<UserPunishLogEntity>()
                        .eq(UserPunishLogEntity::getStatus, 1)
                        .last("LIMIT 20000")
                        .select(UserPunishLogEntity::getUserId)
        );
        Map<Long, Integer> punishCount = new HashMap<>();
        for (UserPunishLogEntity log : punishLogs) {
            if (log.getUserId() == null) {
                continue;
            }
            punishCount.put(log.getUserId(), punishCount.getOrDefault(log.getUserId(), 0) + 1);
        }
        long repeatPunishedUsers = punishCount.values().stream().filter(count -> count >= 2).count();

        return new GovernanceStatsResponse(pendingReports, handledToday, avgHandleHours7d, repeatPunishedUsers);
    }
}

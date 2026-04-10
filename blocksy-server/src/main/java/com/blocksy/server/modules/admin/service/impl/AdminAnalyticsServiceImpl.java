package com.blocksy.server.modules.admin.service.impl;

import com.blocksy.server.config.properties.AnalyticsProperties;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.modules.admin.dto.AnalyticsConfigResponse;
import com.blocksy.server.modules.admin.dto.AnalyticsConfigSaveRequest;
import com.blocksy.server.modules.admin.dto.AnalyticsMetricDefinitionResponse;
import com.blocksy.server.modules.admin.dto.CommunityActivityPointResponse;
import com.blocksy.server.modules.admin.dto.CommunityRankingResponse;
import com.blocksy.server.modules.admin.dto.ContentTrendPointResponse;
import com.blocksy.server.modules.admin.dto.ModerationAnalyticsResponse;
import com.blocksy.server.modules.admin.dto.TrendPointResponse;
import com.blocksy.server.modules.admin.entity.AdminOperationLogEntity;
import com.blocksy.server.modules.admin.entity.PlatformSettingEntity;
import com.blocksy.server.modules.admin.mapper.AdminOperationLogMapper;
import com.blocksy.server.modules.admin.mapper.PlatformSettingMapper;
import com.blocksy.server.modules.admin.service.AdminAnalyticsService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class AdminAnalyticsServiceImpl implements AdminAnalyticsService {

    private static final String GROUP_ANALYTICS = "ANALYTICS";
    private static final String KEY_DEFAULT_WINDOW_DAYS = "default_window_days";
    private static final String KEY_MAX_WINDOW_DAYS = "max_window_days";
    private static final String KEY_ALLOWED_WINDOW_DAYS = "allowed_window_days";

    private final JdbcTemplate jdbcTemplate;
    private final AnalyticsProperties analyticsProperties;
    private final PlatformSettingMapper platformSettingMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;

    public AdminAnalyticsServiceImpl(
            JdbcTemplate jdbcTemplate,
            AnalyticsProperties analyticsProperties,
            PlatformSettingMapper platformSettingMapper,
            AdminOperationLogMapper adminOperationLogMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.analyticsProperties = analyticsProperties;
        this.platformSettingMapper = platformSettingMapper;
        this.adminOperationLogMapper = adminOperationLogMapper;
    }

    @Override
    public AnalyticsConfigResponse getAnalyticsConfig() {
        WindowConfig config = loadWindowConfig();
        return new AnalyticsConfigResponse(
                config.defaultWindowDays(),
                config.maxWindowDays(),
                config.allowedWindowDays(),
                metricDefinitions(),
                config.updatedAt()
        );
    }

    @Override
    public AnalyticsConfigResponse saveAnalyticsConfig(Long adminUserId, AnalyticsConfigSaveRequest request) {
        int maxDays = request.maxWindowDays();
        int defaultDays = request.defaultWindowDays();
        if (defaultDays > maxDays) {
            throw new BusinessException("默认窗口不能大于最大窗口");
        }
        if (maxDays > 365) {
            throw new BusinessException("最大窗口不能超过 365 天");
        }
        List<Integer> allowed = request.allowedWindowDays().stream()
                .filter(item -> item != null && item > 0)
                .distinct()
                .sorted()
                .filter(item -> item <= maxDays)
                .collect(Collectors.toCollection(ArrayList::new));
        if (allowed.isEmpty()) {
            throw new BusinessException("可选窗口不能为空");
        }
        if (!allowed.contains(defaultDays)) {
            allowed.add(defaultDays);
            Collections.sort(allowed);
        }

        LocalDateTime now = LocalDateTime.now();
        upsertSetting(KEY_DEFAULT_WINDOW_DAYS, String.valueOf(defaultDays), "分析默认时间窗口（天）", now);
        upsertSetting(KEY_MAX_WINDOW_DAYS, String.valueOf(maxDays), "分析最大时间窗口（天）", now);
        upsertSetting(
                KEY_ALLOWED_WINDOW_DAYS,
                allowed.stream().map(String::valueOf).collect(Collectors.joining(",")),
                "分析可选时间窗口（逗号分隔）",
                now
        );

        AdminOperationLogEntity opLog = new AdminOperationLogEntity();
        opLog.setModule("ANALYTICS");
        opLog.setAction("UPDATE_WINDOW_CONFIG");
        opLog.setOperatorUserId(adminUserId);
        opLog.setTargetType("ANALYTICS_CONFIG");
        opLog.setTargetId(null);
        opLog.setDetails("default=" + defaultDays + ",max=" + maxDays + ",allowed=" + allowed);
        opLog.setStatus(1);
        opLog.setCreatedAt(now);
        opLog.setUpdatedAt(now);
        adminOperationLogMapper.insert(opLog);

        WindowConfig latest = loadWindowConfig();
        return new AnalyticsConfigResponse(
                latest.defaultWindowDays(),
                latest.maxWindowDays(),
                latest.allowedWindowDays(),
                metricDefinitions(),
                latest.updatedAt()
        );
    }

    @Override
    public List<TrendPointResponse> userGrowthTrend(Integer days, LocalDate startDate, LocalDate endDate) {
        TimeWindow window = resolveWindow(days, startDate, endDate);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT DATE(created_at) AS day, COUNT(1) AS value " +
                        "FROM users WHERE status = 1 AND created_at >= ? AND created_at < ? GROUP BY DATE(created_at) ORDER BY day",
                window.startAt(),
                window.endExclusive()
        );
        Map<String, Long> map = toLongMap(rows, "day", "value");
        return buildTrend(window.startDate(), window.days(), map);
    }

    @Override
    public List<CommunityActivityPointResponse> communityActivity(Integer limit, Integer days, LocalDate startDate, LocalDate endDate) {
        int resolvedLimit = limit == null || limit < 1 ? 20 : Math.min(limit, 100);
        TimeWindow window = resolveWindow(days, startDate, endDate);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT c.id AS community_id, c.name AS community_name, " +
                        "COALESCE(p.post_count, 0) AS post_count, " +
                        "COALESCE(pc.comment_count, 0) AS comment_count, " +
                        "COALESCE(r.report_count, 0) AS report_count " +
                        "FROM communities c " +
                        "LEFT JOIN (SELECT community_id, COUNT(1) AS post_count FROM posts WHERE status = 1 AND created_at >= ? AND created_at < ? GROUP BY community_id) p ON p.community_id = c.id " +
                        "LEFT JOIN (SELECT p.community_id AS community_id, COUNT(1) AS comment_count FROM post_comments pc JOIN posts p ON p.id = pc.post_id WHERE pc.status = 1 AND pc.created_at >= ? AND pc.created_at < ? GROUP BY p.community_id) pc ON pc.community_id = c.id " +
                        "LEFT JOIN (SELECT p.community_id AS community_id, COUNT(1) AS report_count FROM reports r JOIN posts p ON p.id = r.target_id AND r.target_type = 'POST' WHERE r.status = 1 AND r.created_at >= ? AND r.created_at < ? GROUP BY p.community_id) r ON r.community_id = c.id " +
                        "WHERE c.status = 1 ORDER BY post_count DESC, comment_count DESC LIMIT ?",
                window.startAt(),
                window.endExclusive(),
                window.startAt(),
                window.endExclusive(),
                window.startAt(),
                window.endExclusive(),
                resolvedLimit
        );
        return rows.stream().map(row -> new CommunityActivityPointResponse(
                asLong(row.get("community_id")),
                asString(row.get("community_name")),
                asLong(row.get("post_count")),
                asLong(row.get("comment_count")),
                asLong(row.get("report_count"))
        )).toList();
    }

    @Override
    public List<ContentTrendPointResponse> contentTrend(Integer days, LocalDate startDate, LocalDate endDate) {
        TimeWindow window = resolveWindow(days, startDate, endDate);
        List<Map<String, Object>> postRows = jdbcTemplate.queryForList(
                "SELECT DATE(created_at) AS day, COUNT(1) AS value FROM posts WHERE status = 1 AND created_at >= ? AND created_at < ? GROUP BY DATE(created_at)",
                window.startAt(),
                window.endExclusive()
        );
        List<Map<String, Object>> commentRows = jdbcTemplate.queryForList(
                "SELECT DATE(created_at) AS day, COUNT(1) AS value FROM post_comments WHERE status = 1 AND created_at >= ? AND created_at < ? GROUP BY DATE(created_at)",
                window.startAt(),
                window.endExclusive()
        );
        Map<String, Long> postMap = toLongMap(postRows, "day", "value");
        Map<String, Long> commentMap = toLongMap(commentRows, "day", "value");
        List<ContentTrendPointResponse> points = new ArrayList<>();
        for (int i = 0; i < window.days(); i++) {
            LocalDate day = window.startDate().plusDays(i);
            String key = day.toString();
            points.add(new ContentTrendPointResponse(
                    key,
                    postMap.getOrDefault(key, 0L),
                    commentMap.getOrDefault(key, 0L)
            ));
        }
        return points;
    }

    @Override
    public ModerationAnalyticsResponse moderationOverview() {
        long total = count("SELECT COUNT(1) FROM reports WHERE status = 1");
        long pending = count("SELECT COUNT(1) FROM reports WHERE status = 1 AND process_status = 'PENDING'");
        long handled = count("SELECT COUNT(1) FROM reports WHERE status = 1 AND process_status IN ('RESOLVED', 'REJECTED')");
        double pendingRate = total == 0 ? 0D : (double) pending / total;
        double handledRate = total == 0 ? 0D : (double) handled / total;
        return new ModerationAnalyticsResponse(total, pending, handled, pendingRate, handledRate);
    }

    @Override
    public List<TrendPointResponse> retentionTrend(Integer days, LocalDate startDate, LocalDate endDate) {
        TimeWindow window = resolveWindow(days, startDate, endDate);
        List<TrendPointResponse> points = new ArrayList<>();
        for (int i = 0; i < window.days(); i++) {
            LocalDate day = window.startDate().plusDays(i);
            long retained = retainedUsers(day);
            points.add(new TrendPointResponse(day.toString(), retained));
        }
        return points;
    }

    @Override
    public List<CommunityRankingResponse> communityRanking(Integer limit, Integer days, LocalDate startDate, LocalDate endDate) {
        List<CommunityActivityPointResponse> source = communityActivity(limit, days, startDate, endDate);
        return source.stream()
                .map(item -> new CommunityRankingResponse(
                        item.communityId(),
                        item.communityName(),
                        item.postCount(),
                        item.commentCount(),
                        item.reportCount(),
                        item.postCount() * 1D + item.commentCount() * 0.5D - item.reportCount() * 0.3D
                ))
                .sorted(Comparator.comparing(CommunityRankingResponse::score).reversed())
                .toList();
    }

    private TimeWindow resolveWindow(Integer days, LocalDate startDate, LocalDate endDate) {
        WindowConfig windowConfig = loadWindowConfig();
        int defaultWindowDays = windowConfig.defaultWindowDays();
        int maxWindowDays = windowConfig.maxWindowDays();
        if (startDate != null || endDate != null) {
            LocalDate resolvedEnd = endDate == null ? LocalDate.now() : endDate;
            LocalDate resolvedStart = startDate == null ? resolvedEnd.minusDays(defaultWindowDays - 1L) : startDate;
            if (resolvedStart.isAfter(resolvedEnd)) {
                LocalDate temp = resolvedStart;
                resolvedStart = resolvedEnd;
                resolvedEnd = temp;
            }
            int resolvedDays = (int) (ChronoUnit.DAYS.between(resolvedStart, resolvedEnd) + 1);
            int boundedDays = Math.max(1, Math.min(resolvedDays, maxWindowDays));
            LocalDate boundedStart = resolvedEnd.minusDays(boundedDays - 1L);
            return new TimeWindow(boundedStart, resolvedEnd, boundedDays, boundedStart.atStartOfDay(), resolvedEnd.plusDays(1).atStartOfDay());
        }
        int resolvedDays = days == null || days < 1 ? defaultWindowDays : days;
        int boundedDays = Math.min(resolvedDays, maxWindowDays);
        LocalDate resolvedEnd = LocalDate.now();
        LocalDate resolvedStart = resolvedEnd.minusDays(boundedDays - 1L);
        return new TimeWindow(resolvedStart, resolvedEnd, boundedDays, resolvedStart.atStartOfDay(), resolvedEnd.plusDays(1).atStartOfDay());
    }

    private WindowConfig loadWindowConfig() {
        List<PlatformSettingEntity> rows = platformSettingMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlatformSettingEntity>()
                        .eq(PlatformSettingEntity::getSettingGroup, GROUP_ANALYTICS)
                        .eq(PlatformSettingEntity::getStatus, 1)
        );
        Map<String, PlatformSettingEntity> map = new HashMap<>();
        for (PlatformSettingEntity row : rows) {
            if (row.getSettingKey() == null) {
                continue;
            }
            map.put(row.getSettingKey(), row);
        }
        int maxDays = parseIntOrDefault(
                map.get(KEY_MAX_WINDOW_DAYS) == null ? null : map.get(KEY_MAX_WINDOW_DAYS).getSettingValue(),
                analyticsProperties.getMaxWindowDays()
        );
        if (maxDays < 1) {
            maxDays = analyticsProperties.getMaxWindowDays();
        }
        int defaultDays = parseIntOrDefault(
                map.get(KEY_DEFAULT_WINDOW_DAYS) == null ? null : map.get(KEY_DEFAULT_WINDOW_DAYS).getSettingValue(),
                analyticsProperties.getDefaultWindowDays()
        );
        if (defaultDays < 1) {
            defaultDays = analyticsProperties.getDefaultWindowDays();
        }
        if (defaultDays > maxDays) {
            defaultDays = maxDays;
        }
        String allowedRaw = map.get(KEY_ALLOWED_WINDOW_DAYS) == null ? null : map.get(KEY_ALLOWED_WINDOW_DAYS).getSettingValue();
        List<Integer> allowed = parseAllowedWindows(allowedRaw, maxDays);
        if (!allowed.contains(defaultDays)) {
            allowed.add(defaultDays);
            Collections.sort(allowed);
        }
        LocalDateTime updatedAt = rows.stream()
                .map(PlatformSettingEntity::getUpdatedAt)
                .filter(item -> item != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        return new WindowConfig(defaultDays, maxDays, allowed, updatedAt);
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignore) {
            return defaultValue;
        }
    }

    private List<Integer> parseAllowedWindows(String raw, int maxDays) {
        if (raw == null || raw.isBlank()) {
            List<Integer> defaults = new ArrayList<>();
            defaults.add(7);
            if (maxDays >= 14) {
                defaults.add(14);
            }
            if (maxDays >= 30) {
                defaults.add(30);
            }
            if (maxDays >= 90) {
                defaults.add(90);
            }
            return defaults.stream().filter(item -> item <= maxDays).distinct().sorted().toList();
        }
        return java.util.Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .map(item -> {
                    try {
                        return Integer.parseInt(item);
                    } catch (Exception ignore) {
                        return null;
                    }
                })
                .filter(item -> item != null && item > 0 && item <= maxDays)
                .distinct()
                .sorted()
                .toList();
    }

    private void upsertSetting(String key, String value, String description, LocalDateTime now) {
        PlatformSettingEntity existing = platformSettingMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlatformSettingEntity>()
                        .eq(PlatformSettingEntity::getSettingGroup, GROUP_ANALYTICS)
                        .eq(PlatformSettingEntity::getSettingKey, key)
                        .last("LIMIT 1")
        );
        if (existing == null) {
            PlatformSettingEntity row = new PlatformSettingEntity();
            row.setSettingGroup(GROUP_ANALYTICS);
            row.setSettingKey(key);
            row.setSettingValue(value);
            row.setDescription(description);
            row.setStatus(1);
            row.setCreatedAt(now);
            row.setUpdatedAt(now);
            platformSettingMapper.insert(row);
            return;
        }
        existing.setSettingValue(value);
        existing.setDescription(description);
        existing.setStatus(1);
        existing.setUpdatedAt(now);
        platformSettingMapper.updateById(existing);
    }

    private List<AnalyticsMetricDefinitionResponse> metricDefinitions() {
        return List.of(
                new AnalyticsMetricDefinitionResponse(
                        "USER_GROWTH_DAILY",
                        "新增用户数（日）",
                        "统计窗口内每天新增且状态为正常（status=1）的用户数量。",
                        "count(users where status=1 and DATE(created_at)=day)",
                        "users"
                ),
                new AnalyticsMetricDefinitionResponse(
                        "COMMUNITY_ACTIVITY",
                        "社区活跃度",
                        "按社区聚合帖子数、评论数、举报数，用于衡量社区活跃水平。",
                        "post_count + comment_count - report_count(展示为拆分字段)",
                        "communities, posts, post_comments, reports"
                ),
                new AnalyticsMetricDefinitionResponse(
                        "CONTENT_TREND",
                        "内容活跃趋势",
                        "统计窗口内每天新增帖子数与评论数。",
                        "post_count(day), comment_count(day)",
                        "posts, post_comments"
                ),
                new AnalyticsMetricDefinitionResponse(
                        "RETENTION_D1",
                        "次日留存人数",
                        "某日活跃用户集合与次日活跃用户集合的交集人数。",
                        "|active_users(day) ∩ active_users(day+1)|",
                        "user_behavior_logs, posts, post_comments, listings, event_signups"
                ),
                new AnalyticsMetricDefinitionResponse(
                        "COMMUNITY_RANKING_SCORE",
                        "社区综合分",
                        "用于热门社区排行的简化评分口径。",
                        "score = post_count * 1.0 + comment_count * 0.5 - report_count * 0.3",
                        "posts, post_comments, reports"
                ),
                new AnalyticsMetricDefinitionResponse(
                        "MODERATION_OVERVIEW",
                        "审核与举报概览",
                        "统计举报总量、待处理量、已处理量及对应比率。",
                        "pending_rate=pending/total, handled_rate=handled/total",
                        "reports"
                )
        );
    }

    private long count(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }

    private Map<String, Long> toLongMap(List<Map<String, Object>> rows, String keyField, String valueField) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String key = row.get(keyField).toString();
            map.put(key, asLong(row.get(valueField)));
        }
        return map;
    }

    private List<TrendPointResponse> buildTrend(LocalDate start, int days, Map<String, Long> valueMap) {
        List<TrendPointResponse> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate day = start.plusDays(i);
            String key = day.toString();
            result.add(new TrendPointResponse(key, valueMap.getOrDefault(key, 0L)));
        }
        return result;
    }

    private Long asLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private String asString(Object value) {
        return value == null ? "" : value.toString();
    }

    private long retainedUsers(LocalDate day) {
        LocalDate nextDay = day.plusDays(1);
        Long value = jdbcTemplate.queryForObject(
                "WITH day_users AS (" +
                        "SELECT DISTINCT user_id FROM (" +
                        "SELECT user_id FROM user_behavior_logs WHERE status = 1 AND created_at >= ? AND created_at < ? " +
                        "UNION ALL SELECT user_id FROM posts WHERE status = 1 AND created_at >= ? AND created_at < ? " +
                        "UNION ALL SELECT user_id FROM post_comments WHERE status = 1 AND created_at >= ? AND created_at < ? " +
                        "UNION ALL SELECT user_id FROM listings WHERE status = 1 AND created_at >= ? AND created_at < ? " +
                        "UNION ALL SELECT user_id FROM event_signups WHERE status = 1 AND created_at >= ? AND created_at < ? " +
                        ") t WHERE user_id IS NOT NULL" +
                        "), next_day_users AS (" +
                        "SELECT DISTINCT user_id FROM (" +
                        "SELECT user_id FROM user_behavior_logs WHERE status = 1 AND created_at >= ? AND created_at < ? " +
                        "UNION ALL SELECT user_id FROM posts WHERE status = 1 AND created_at >= ? AND created_at < ? " +
                        "UNION ALL SELECT user_id FROM post_comments WHERE status = 1 AND created_at >= ? AND created_at < ? " +
                        "UNION ALL SELECT user_id FROM listings WHERE status = 1 AND created_at >= ? AND created_at < ? " +
                        "UNION ALL SELECT user_id FROM event_signups WHERE status = 1 AND created_at >= ? AND created_at < ? " +
                        ") t WHERE user_id IS NOT NULL" +
                        ") SELECT COUNT(1) FROM day_users d JOIN next_day_users n ON n.user_id = d.user_id",
                Long.class,
                day.atStartOfDay(), nextDay.atStartOfDay(),
                day.atStartOfDay(), nextDay.atStartOfDay(),
                day.atStartOfDay(), nextDay.atStartOfDay(),
                day.atStartOfDay(), nextDay.atStartOfDay(),
                day.atStartOfDay(), nextDay.atStartOfDay(),
                nextDay.atStartOfDay(), nextDay.plusDays(1).atStartOfDay(),
                nextDay.atStartOfDay(), nextDay.plusDays(1).atStartOfDay(),
                nextDay.atStartOfDay(), nextDay.plusDays(1).atStartOfDay(),
                nextDay.atStartOfDay(), nextDay.plusDays(1).atStartOfDay(),
                nextDay.atStartOfDay(), nextDay.plusDays(1).atStartOfDay()
        );
        return value == null ? 0L : value;
    }

    private record TimeWindow(
            LocalDate startDate,
            LocalDate endDate,
            int days,
            LocalDateTime startAt,
            LocalDateTime endExclusive
    ) {
    }

    private record WindowConfig(
            int defaultWindowDays,
            int maxWindowDays,
            List<Integer> allowedWindowDays,
            LocalDateTime updatedAt
    ) {
    }
}

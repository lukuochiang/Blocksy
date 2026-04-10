package com.blocksy.server.modules.admin.service;

import com.blocksy.server.modules.admin.dto.CommunityActivityPointResponse;
import com.blocksy.server.modules.admin.dto.AnalyticsConfigResponse;
import com.blocksy.server.modules.admin.dto.AnalyticsConfigSaveRequest;
import com.blocksy.server.modules.admin.dto.CommunityRankingResponse;
import com.blocksy.server.modules.admin.dto.ContentTrendPointResponse;
import com.blocksy.server.modules.admin.dto.ModerationAnalyticsResponse;
import com.blocksy.server.modules.admin.dto.TrendPointResponse;

import java.time.LocalDate;
import java.util.List;

public interface AdminAnalyticsService {

    AnalyticsConfigResponse getAnalyticsConfig();

    AnalyticsConfigResponse saveAnalyticsConfig(Long adminUserId, AnalyticsConfigSaveRequest request);

    List<TrendPointResponse> userGrowthTrend(Integer days, LocalDate startDate, LocalDate endDate);

    List<CommunityActivityPointResponse> communityActivity(Integer limit, Integer days, LocalDate startDate, LocalDate endDate);

    List<ContentTrendPointResponse> contentTrend(Integer days, LocalDate startDate, LocalDate endDate);

    ModerationAnalyticsResponse moderationOverview();

    List<TrendPointResponse> retentionTrend(Integer days, LocalDate startDate, LocalDate endDate);

    List<CommunityRankingResponse> communityRanking(Integer limit, Integer days, LocalDate startDate, LocalDate endDate);
}

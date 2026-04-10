package com.blocksy.server.modules.admin.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.admin.dto.AnalyticsConfigResponse;
import com.blocksy.server.modules.admin.dto.AnalyticsConfigSaveRequest;
import com.blocksy.server.modules.admin.dto.CommunityActivityPointResponse;
import com.blocksy.server.modules.admin.dto.CommunityRankingResponse;
import com.blocksy.server.modules.admin.dto.ContentTrendPointResponse;
import com.blocksy.server.modules.admin.dto.ModerationAnalyticsResponse;
import com.blocksy.server.modules.admin.dto.TrendPointResponse;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import com.blocksy.server.modules.admin.service.AdminAnalyticsService;
import com.blocksy.server.security.service.AdminGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@Tag(name = "Admin-Analytics", description = "后台数据分析")
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;
    private final AdminGuard adminGuard;

    public AdminAnalyticsController(AdminAnalyticsService adminAnalyticsService, AdminGuard adminGuard) {
        this.adminAnalyticsService = adminAnalyticsService;
        this.adminGuard = adminGuard;
    }

    @GetMapping("/config")
    @Operation(summary = "获取分析口径配置与指标定义")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AnalyticsConfigResponse> config() {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminAnalyticsService.getAnalyticsConfig());
    }

    @PostMapping("/config")
    @Operation(summary = "保存分析口径配置")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AnalyticsConfigResponse> saveConfig(@Valid @RequestBody AnalyticsConfigSaveRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(adminAnalyticsService.saveAnalyticsConfig(admin.userId(), request));
    }

    @GetMapping("/growth")
    @Operation(summary = "用户增长趋势")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<TrendPointResponse>> growth(
            @RequestParam(value = "days", required = false) Integer days,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminAnalyticsService.userGrowthTrend(days, startDate, endDate));
    }

    @GetMapping("/community")
    @Operation(summary = "社区活跃分析")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<CommunityActivityPointResponse>> community(
            @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit,
            @RequestParam(value = "days", required = false) Integer days,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminAnalyticsService.communityActivity(limit, days, startDate, endDate));
    }

    @GetMapping("/content")
    @Operation(summary = "内容活跃趋势")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<ContentTrendPointResponse>> content(
            @RequestParam(value = "days", required = false) Integer days,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminAnalyticsService.contentTrend(days, startDate, endDate));
    }

    @GetMapping("/moderation")
    @Operation(summary = "审核与举报分析")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ModerationAnalyticsResponse> moderation() {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminAnalyticsService.moderationOverview());
    }

    @GetMapping("/retention")
    @Operation(summary = "留存趋势（MVP 近似）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<TrendPointResponse>> retention(
            @RequestParam(value = "days", required = false) Integer days,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminAnalyticsService.retentionTrend(days, startDate, endDate));
    }

    @GetMapping("/ranking")
    @Operation(summary = "热门社区排行")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<CommunityRankingResponse>> ranking(
            @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit,
            @RequestParam(value = "days", required = false) Integer days,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminAnalyticsService.communityRanking(limit, days, startDate, endDate));
    }
}

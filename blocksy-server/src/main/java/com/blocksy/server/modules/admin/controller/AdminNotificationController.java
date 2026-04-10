package com.blocksy.server.modules.admin.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.admin.dto.GovernanceStatsResponse;
import com.blocksy.server.modules.admin.dto.NotificationTemplateResponse;
import com.blocksy.server.modules.admin.dto.NotificationTemplateSaveRequest;
import com.blocksy.server.modules.admin.dto.PushRecordResponse;
import com.blocksy.server.modules.admin.dto.PushTaskCreateRequest;
import com.blocksy.server.modules.admin.dto.PushTaskResponse;
import com.blocksy.server.modules.admin.service.AdminMessagingService;
import com.blocksy.server.modules.admin.service.GovernanceStatsService;
import com.blocksy.server.modules.notification.dto.AdminAnnouncementItemResponse;
import com.blocksy.server.modules.notification.dto.NotificationTrendPointResponse;
import com.blocksy.server.modules.notification.dto.NotificationStatsResponse;
import com.blocksy.server.modules.notification.dto.SystemAnnouncementRequest;
import com.blocksy.server.modules.notification.service.NotificationService;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import com.blocksy.server.security.service.AdminGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
@Tag(name = "Admin-Notification", description = "后台通知与公告管理")
public class AdminNotificationController {

    private final NotificationService notificationService;
    private final GovernanceStatsService governanceStatsService;
    private final AdminMessagingService adminMessagingService;
    private final AdminGuard adminGuard;

    public AdminNotificationController(
            NotificationService notificationService,
            GovernanceStatsService governanceStatsService,
            AdminMessagingService adminMessagingService,
            AdminGuard adminGuard
    ) {
        this.notificationService = notificationService;
        this.governanceStatsService = governanceStatsService;
        this.adminMessagingService = adminMessagingService;
        this.adminGuard = adminGuard;
    }

    @PostMapping("/system-announcement")
    @Operation(summary = "发布系统公告并下发站内通知")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Integer> systemAnnouncement(@Valid @RequestBody SystemAnnouncementRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(
                notificationService.createSystemAnnouncement(admin.userId(), request.title(), request.content())
        );
    }

    @GetMapping("/stats")
    @Operation(summary = "通知触达统计")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<NotificationStatsResponse> stats() {
        adminGuard.requireAdmin();
        return ApiResponse.success(notificationService.stats());
    }

    @GetMapping("/announcements")
    @Operation(summary = "公告列表（分页/筛选）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<AdminAnnouncementItemResponse>> announcements(
            @Parameter(description = "关键词（标题/内容）")
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "状态：1=有效，0=已撤回")
            @RequestParam(value = "status", required = false) Integer status,
            @Parameter(description = "页码，从1开始")
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页大小")
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(notificationService.pageAnnouncements(keyword, status, page, pageSize));
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "撤回公告")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> revoke(@PathVariable("id") Long id) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        notificationService.revokeAnnouncement(id, admin.userId());
        return ApiResponse.success();
    }

    @PostMapping("/{id}/redispatch")
    @Operation(summary = "公告二次下发")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Integer> redispatch(@PathVariable("id") Long id) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(notificationService.redispatchAnnouncement(id, admin.userId()));
    }

    @GetMapping("/trend")
    @Operation(summary = "通知趋势图（7/30天）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<NotificationTrendPointResponse>> trend(
            @Parameter(description = "天数，支持7或30")
            @RequestParam(value = "days", required = false, defaultValue = "7") Integer days
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(notificationService.trend(days));
    }

    @GetMapping("/push/tasks")
    @Operation(summary = "Push 任务列表（分页）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<PushTaskResponse>> pushTasks(
            @RequestParam(value = "taskStatus", required = false) String taskStatus,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminMessagingService.pagePushTasks(taskStatus, keyword, page, pageSize));
    }

    @PostMapping("/push/tasks")
    @Operation(summary = "创建 Push 任务")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PushTaskResponse> createPushTask(@Valid @RequestBody PushTaskCreateRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(adminMessagingService.createPushTask(admin.userId(), request));
    }

    @PostMapping("/push/tasks/{id}/send")
    @Operation(summary = "立即下发 Push 任务")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Integer> sendPushTask(@PathVariable("id") Long id) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(adminMessagingService.sendPushTask(id, admin.userId()));
    }

    @GetMapping("/push/records")
    @Operation(summary = "Push 发送记录（分页）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<PushRecordResponse>> pushRecords(
            @RequestParam(value = "taskId", required = false) Long taskId,
            @RequestParam(value = "sendStatus", required = false) String sendStatus,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminMessagingService.pagePushRecords(taskId, sendStatus, page, pageSize));
    }

    @GetMapping("/templates")
    @Operation(summary = "通知模板列表（分页）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<NotificationTemplateResponse>> templates(
            @RequestParam(value = "module", required = false) String module,
            @RequestParam(value = "enabled", required = false) Boolean enabled,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminMessagingService.pageNotificationTemplates(module, enabled, page, pageSize));
    }

    @PostMapping("/templates/save")
    @Operation(summary = "新增/更新通知模板")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<NotificationTemplateResponse> saveTemplate(@Valid @RequestBody NotificationTemplateSaveRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(adminMessagingService.saveNotificationTemplate(admin.userId(), request));
    }

    @GetMapping("/governance-stats")
    @Operation(summary = "治理看板关键指标")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<GovernanceStatsResponse> governanceStats() {
        adminGuard.requireAdmin();
        return ApiResponse.success(governanceStatsService.getOverview());
    }
}

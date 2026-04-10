package com.blocksy.server.modules.admin.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.admin.dto.AdminEventSignupResponse;
import com.blocksy.server.modules.admin.service.AdminEventSignupService;
import com.blocksy.server.modules.event.dto.AdminEventHandleLogResponse;
import com.blocksy.server.modules.event.dto.AdminEventHandleRequest;
import com.blocksy.server.modules.event.dto.AdminEventBatchHandleRequest;
import com.blocksy.server.modules.event.dto.AdminEventBatchHandleResponse;
import com.blocksy.server.modules.event.dto.AdminEventBatchRetryRequest;
import com.blocksy.server.modules.event.dto.AdminEventResponse;
import com.blocksy.server.modules.event.service.EventService;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import com.blocksy.server.security.service.AdminGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/events")
@Tag(name = "Admin-Event", description = "后台活动管理")
public class AdminEventController {

    private final EventService eventService;
    private final AdminEventSignupService adminEventSignupService;
    private final AdminGuard adminGuard;

    public AdminEventController(EventService eventService, AdminEventSignupService adminEventSignupService, AdminGuard adminGuard) {
        this.eventService = eventService;
        this.adminEventSignupService = adminEventSignupService;
        this.adminGuard = adminGuard;
    }

    @GetMapping
    @Operation(summary = "后台活动列表")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<AdminEventResponse>> list(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "communityId", required = false) Long communityId,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(eventService.listForAdmin(status, communityId, keyword));
    }

    @GetMapping("/signups")
    @Operation(summary = "活动报名记录（分页）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<AdminEventSignupResponse>> signups(
            @RequestParam(value = "eventId", required = false) Long eventId,
            @RequestParam(value = "communityId", required = false) Long communityId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminEventSignupService.page(eventId, communityId, userId, page, pageSize));
    }

    @PostMapping("/{id}/handle")
    @Operation(summary = "后台处理活动（下架/恢复/删除）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AdminEventResponse> handle(
            @PathVariable("id") Long id,
            @Valid @RequestBody AdminEventHandleRequest request
    ) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(eventService.handleForAdmin(id, admin.userId(), request.action(), request.note()));
    }

    @PostMapping("/batch-handle")
    @Operation(summary = "后台批量处理活动")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AdminEventBatchHandleResponse> batchHandle(@Valid @RequestBody AdminEventBatchHandleRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(eventService.batchHandleForAdmin(admin.userId(), request));
    }

    @PostMapping("/batch-retry")
    @Operation(summary = "后台重试批量处理活动失败项")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AdminEventBatchHandleResponse> batchRetry(@Valid @RequestBody AdminEventBatchRetryRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(eventService.batchRetryForAdmin(admin.userId(), request));
    }

    @PostMapping("/batch-retry/export")
    @Operation(summary = "重试活动失败项并导出结果 CSV")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> retryAndExport(@Valid @RequestBody AdminEventBatchRetryRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        AdminEventBatchHandleResponse response = eventService.batchRetryForAdmin(admin.userId(), request);
        String csv = eventService.exportBatchHandleResultCsv(response);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=event-batch-retry-result.csv")
                .contentType(MediaType.valueOf("text/csv;charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/{id}/logs")
    @Operation(summary = "后台获取活动处理日志")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<AdminEventHandleLogResponse>> logs(@PathVariable("id") Long id) {
        adminGuard.requireAdmin();
        return ApiResponse.success(eventService.listHandleLogsForAdmin(id));
    }

    @GetMapping("/handle-logs")
    @Operation(summary = "后台查询活动处理日志（支持筛选）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<AdminEventHandleLogResponse>> handleLogs(
            @RequestParam(value = "eventId", required = false) Long eventId,
            @RequestParam(value = "operatorUserId", required = false) Long operatorUserId,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "startAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(value = "endAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(eventService.listHandleLogsForAdmin(eventId, operatorUserId, action, startAt, endAt));
    }

    @GetMapping("/handle-logs/export")
    @Operation(summary = "导出活动处理日志 CSV")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> exportHandleLogs(
            @RequestParam(value = "eventId", required = false) Long eventId,
            @RequestParam(value = "operatorUserId", required = false) Long operatorUserId,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "startAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(value = "endAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt
    ) {
        adminGuard.requireAdmin();
        String csv = eventService.exportHandleLogsCsv(eventId, operatorUserId, action, startAt, endAt);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=event-handle-logs.csv")
                .contentType(MediaType.valueOf("text/csv;charset=UTF-8"))
                .body(csv);
    }
}

package com.blocksy.server.modules.notification.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.notification.dto.NotificationBatchReadRequest;
import com.blocksy.server.modules.notification.dto.NotificationPageResponse;
import com.blocksy.server.modules.notification.dto.NotificationUnreadCountResponse;
import com.blocksy.server.modules.notification.dto.NotificationResponse;
import com.blocksy.server.modules.notification.service.NotificationService;
import com.blocksy.server.security.SecurityUtils;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "通知接口")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "获取我的通知")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<NotificationResponse>> list() {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(notificationService.listByUserId(currentUser.userId()));
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取我的通知（支持类型/已读筛选）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<NotificationPageResponse> page(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "isRead", required = false) Boolean isRead
    ) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(notificationService.pageByUserId(currentUser.userId(), page, pageSize, type, isRead));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取我的通知未读数")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<NotificationUnreadCountResponse> unreadCount() {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(new NotificationUnreadCountResponse(notificationService.countUnread(currentUser.userId())));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "将单条通知标记为已读")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Boolean> markRead(@PathVariable("id") Long id) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        notificationService.markRead(currentUser.userId(), id);
        return ApiResponse.success(Boolean.TRUE);
    }

    @PostMapping("/read-batch")
    @Operation(summary = "批量将通知标记为已读")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Integer> markReadBatch(@Valid @RequestBody NotificationBatchReadRequest request) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(notificationService.markReadBatch(currentUser.userId(), request.notificationIds()));
    }

    @PostMapping("/read-all")
    @Operation(summary = "将当前用户全部通知标记为已读")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Integer> markAllRead() {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(notificationService.markAllRead(currentUser.userId()));
    }
}

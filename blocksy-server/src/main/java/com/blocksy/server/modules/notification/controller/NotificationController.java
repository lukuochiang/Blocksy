package com.blocksy.server.modules.notification.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.notification.dto.NotificationResponse;
import com.blocksy.server.modules.notification.service.NotificationService;
import com.blocksy.server.security.SecurityUtils;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}

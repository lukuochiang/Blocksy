package com.blocksy.server.modules.admin.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.user.dto.AdminUserResponse;
import com.blocksy.server.modules.user.service.UserService;
import com.blocksy.server.security.service.AdminGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin-User", description = "后台用户管理")
public class AdminUserController {

    private final UserService userService;
    private final AdminGuard adminGuard;

    public AdminUserController(UserService userService, AdminGuard adminGuard) {
        this.userService = userService;
        this.adminGuard = adminGuard;
    }

    @GetMapping
    @Operation(summary = "后台用户列表")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<AdminUserResponse>> list() {
        adminGuard.requireAdmin();
        return ApiResponse.success(userService.listActiveUsers().stream().map(user -> new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getStatus(),
                user.getCreatedAt()
        )).toList());
    }

    @PostMapping("/{id}/ban")
    @Operation(summary = "封禁用户")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> ban(@PathVariable("id") Long id) {
        adminGuard.requireAdmin();
        userService.banUser(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/unban")
    @Operation(summary = "解封用户")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> unban(@PathVariable("id") Long id) {
        adminGuard.requireAdmin();
        userService.unbanUser(id);
        return ApiResponse.success();
    }
}

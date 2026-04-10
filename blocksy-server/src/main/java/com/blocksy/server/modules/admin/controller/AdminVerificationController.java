package com.blocksy.server.modules.admin.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.admin.dto.UserVerificationHandleRequest;
import com.blocksy.server.modules.admin.dto.UserVerificationResponse;
import com.blocksy.server.modules.admin.service.AdminVerificationService;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import com.blocksy.server.security.service.AdminGuard;
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

@RestController
@RequestMapping("/api/admin/verifications")
@Tag(name = "Admin-Verification", description = "后台认证审核")
public class AdminVerificationController {

    private final AdminVerificationService adminVerificationService;
    private final AdminGuard adminGuard;

    public AdminVerificationController(AdminVerificationService adminVerificationService, AdminGuard adminGuard) {
        this.adminVerificationService = adminVerificationService;
        this.adminGuard = adminGuard;
    }

    @GetMapping
    @Operation(summary = "认证申请列表（分页）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<UserVerificationResponse>> page(
            @RequestParam(value = "processStatus", required = false) String processStatus,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminVerificationService.page(processStatus, userId, page, pageSize));
    }

    @PostMapping("/{id}/handle")
    @Operation(summary = "处理认证申请")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<UserVerificationResponse> handle(@PathVariable("id") Long id, @Valid @RequestBody UserVerificationHandleRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(adminVerificationService.handle(id, admin.userId(), request));
    }
}

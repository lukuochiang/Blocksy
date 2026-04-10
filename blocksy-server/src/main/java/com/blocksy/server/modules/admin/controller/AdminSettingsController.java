package com.blocksy.server.modules.admin.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.admin.dto.PlatformSettingResponse;
import com.blocksy.server.modules.admin.dto.PlatformSettingSaveRequest;
import com.blocksy.server.modules.admin.dto.PolicyDocumentResponse;
import com.blocksy.server.modules.admin.dto.PolicyDocumentSaveRequest;
import com.blocksy.server.modules.admin.service.AdminSettingsService;
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

import java.util.List;

@RestController
@RequestMapping("/api/admin/settings")
@Tag(name = "Admin-Settings", description = "后台系统设置")
public class AdminSettingsController {

    private final AdminSettingsService adminSettingsService;
    private final AdminGuard adminGuard;

    public AdminSettingsController(AdminSettingsService adminSettingsService, AdminGuard adminGuard) {
        this.adminSettingsService = adminSettingsService;
        this.adminGuard = adminGuard;
    }

    @GetMapping("/items")
    @Operation(summary = "平台设置项列表")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<PlatformSettingResponse>> items(
            @RequestParam(value = "settingGroup", required = false) String settingGroup
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminSettingsService.listSettings(settingGroup));
    }

    @PostMapping("/items/save")
    @Operation(summary = "新增/更新平台设置项")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PlatformSettingResponse> saveItem(@Valid @RequestBody PlatformSettingSaveRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(adminSettingsService.saveSetting(admin.userId(), request));
    }

    @GetMapping("/policies")
    @Operation(summary = "协议政策列表")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<PolicyDocumentResponse>> policies(
            @RequestParam(value = "policyType", required = false) String policyType
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminSettingsService.listPolicies(policyType));
    }

    @PostMapping("/policies/save")
    @Operation(summary = "新增协议政策版本")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PolicyDocumentResponse> savePolicy(@Valid @RequestBody PolicyDocumentSaveRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(adminSettingsService.savePolicy(admin.userId(), request));
    }

    @PostMapping("/policies/{id}/activate")
    @Operation(summary = "激活协议政策版本")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> activatePolicy(@PathVariable("id") Long id) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        adminSettingsService.activatePolicy(admin.userId(), id);
        return ApiResponse.success();
    }
}

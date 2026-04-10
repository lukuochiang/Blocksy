package com.blocksy.server.modules.admin.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.admin.dto.AdminOperationLogResponse;
import com.blocksy.server.modules.admin.dto.DataPermissionAssignRequest;
import com.blocksy.server.modules.admin.dto.DataPermissionItemResponse;
import com.blocksy.server.modules.admin.dto.MenuPermissionAssignRequest;
import com.blocksy.server.modules.admin.dto.MenuPermissionItemResponse;
import com.blocksy.server.modules.admin.dto.PermissionOpLogResponse;
import com.blocksy.server.modules.admin.dto.UserBehaviorLogResponse;
import com.blocksy.server.modules.admin.service.AdminPermissionService;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import com.blocksy.server.security.service.AdminGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/permissions")
@Tag(name = "Admin-Permission", description = "后台菜单权限管理")
public class AdminPermissionController {

    private final AdminPermissionService adminPermissionService;
    private final AdminGuard adminGuard;

    public AdminPermissionController(AdminPermissionService adminPermissionService, AdminGuard adminGuard) {
        this.adminPermissionService = adminPermissionService;
        this.adminGuard = adminGuard;
    }

    @GetMapping("/menus")
    @Operation(summary = "菜单权限列表（分页）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<MenuPermissionItemResponse>> menus(
            @RequestParam(value = "roleCode", required = false) String roleCode,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminPermissionService.pageMenuPermissions(roleCode, keyword, page, pageSize));
    }

    @PostMapping("/menus/assign")
    @Operation(summary = "按角色分配菜单权限")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Integer> assignMenus(@Valid @RequestBody MenuPermissionAssignRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(adminPermissionService.assignRoleMenus(admin.userId(), request));
    }

    @GetMapping("/logs")
    @Operation(summary = "菜单权限操作日志")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<PermissionOpLogResponse>> logs(
            @RequestParam(value = "roleCode", required = false) String roleCode,
            @RequestParam(value = "limit", required = false, defaultValue = "50") Integer limit
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminPermissionService.listPermissionLogs(roleCode, limit));
    }

    @GetMapping("/data")
    @Operation(summary = "数据权限列表（分页）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<DataPermissionItemResponse>> data(
            @RequestParam(value = "roleCode", required = false) String roleCode,
            @RequestParam(value = "dataScope", required = false) String dataScope,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminPermissionService.pageDataPermissions(roleCode, dataScope, page, pageSize));
    }

    @PostMapping("/data/assign")
    @Operation(summary = "分配数据权限")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Integer> assignData(@Valid @RequestBody DataPermissionAssignRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(adminPermissionService.assignDataPermissions(admin.userId(), request));
    }

    @GetMapping("/operation-logs")
    @Operation(summary = "后台操作日志（分页）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<AdminOperationLogResponse>> operationLogs(
            @RequestParam(value = "module", required = false) String module,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminPermissionService.pageOperationLogs(module, action, page, pageSize));
    }

    @GetMapping("/user-behavior-logs")
    @Operation(summary = "用户行为日志（分页）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<UserBehaviorLogResponse>> userBehaviorLogs(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "behaviorType", required = false) String behaviorType,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminPermissionService.pageUserBehaviorLogs(userId, behaviorType, page, pageSize));
    }
}

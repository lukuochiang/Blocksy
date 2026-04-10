package com.blocksy.server.modules.admin.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.admin.dto.CommunityNoticeCreateRequest;
import com.blocksy.server.modules.admin.dto.CommunityNoticeResponse;
import com.blocksy.server.modules.admin.service.AdminCommunityNoticeService;
import com.blocksy.server.modules.community.dto.AdminCommunityCreateRequest;
import com.blocksy.server.modules.community.entity.CommunityEntity;
import com.blocksy.server.modules.community.service.CommunityService;
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
@RequestMapping("/api/admin/communities")
@Tag(name = "Admin-Community", description = "后台社区管理")
public class AdminCommunityController {

    private final CommunityService communityService;
    private final AdminCommunityNoticeService adminCommunityNoticeService;
    private final AdminGuard adminGuard;

    public AdminCommunityController(
            CommunityService communityService,
            AdminCommunityNoticeService adminCommunityNoticeService,
            AdminGuard adminGuard
    ) {
        this.communityService = communityService;
        this.adminCommunityNoticeService = adminCommunityNoticeService;
        this.adminGuard = adminGuard;
    }

    @GetMapping
    @Operation(summary = "后台社区列表")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<CommunityEntity>> list() {
        adminGuard.requireAdmin();
        return ApiResponse.success(communityService.listForAdmin());
    }

    @PostMapping
    @Operation(summary = "后台新增社区")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<CommunityEntity> create(@Valid @RequestBody AdminCommunityCreateRequest request) {
        adminGuard.requireAdmin();
        return ApiResponse.success(communityService.createForAdmin(request));
    }

    @GetMapping("/notices")
    @Operation(summary = "社区公告列表（分页）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<CommunityNoticeResponse>> notices(
            @RequestParam(value = "communityId", required = false) Long communityId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminCommunityNoticeService.page(communityId, status, keyword, page, pageSize));
    }

    @PostMapping("/notices")
    @Operation(summary = "发布社区公告")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<CommunityNoticeResponse> createNotice(@Valid @RequestBody CommunityNoticeCreateRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(adminCommunityNoticeService.create(admin.userId(), request));
    }

    @PostMapping("/notices/{id}/revoke")
    @Operation(summary = "撤回社区公告")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<CommunityNoticeResponse> revokeNotice(@PathVariable("id") Long id) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(adminCommunityNoticeService.revoke(id, admin.userId()));
    }
}

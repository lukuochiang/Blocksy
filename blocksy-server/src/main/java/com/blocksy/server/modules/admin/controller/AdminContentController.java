package com.blocksy.server.modules.admin.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.admin.dto.CommunityEngagementResponse;
import com.blocksy.server.modules.admin.dto.ContentCategoryCreateRequest;
import com.blocksy.server.modules.admin.dto.ContentCategoryResponse;
import com.blocksy.server.modules.admin.dto.ContentCategoryToggleRequest;
import com.blocksy.server.modules.admin.dto.MediaAssetResponse;
import com.blocksy.server.modules.admin.service.AdminContentOpsService;
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
@RequestMapping("/api/admin/content")
@Tag(name = "Admin-Content", description = "后台内容运营管理")
public class AdminContentController {

    private final AdminContentOpsService adminContentOpsService;
    private final AdminGuard adminGuard;

    public AdminContentController(AdminContentOpsService adminContentOpsService, AdminGuard adminGuard) {
        this.adminContentOpsService = adminContentOpsService;
        this.adminGuard = adminGuard;
    }

    @GetMapping("/communities/engagement")
    @Operation(summary = "社区成员活跃（分页）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<CommunityEngagementResponse>> communityEngagement(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminContentOpsService.pageCommunityEngagement(keyword, page, pageSize));
    }

    @GetMapping("/categories")
    @Operation(summary = "分类管理列表（分页）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<ContentCategoryResponse>> categories(
            @RequestParam(value = "module", required = false) String module,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminContentOpsService.pageCategories(module, keyword, page, pageSize));
    }

    @PostMapping("/categories")
    @Operation(summary = "新增分类")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ContentCategoryResponse> createCategory(@Valid @RequestBody ContentCategoryCreateRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(adminContentOpsService.createCategory(admin.userId(), request));
    }

    @PostMapping("/categories/{id}/toggle")
    @Operation(summary = "启停分类")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ContentCategoryResponse> toggleCategory(
            @PathVariable("id") Long id,
            @Valid @RequestBody ContentCategoryToggleRequest request
    ) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(adminContentOpsService.toggleCategory(id, admin.userId(), request.enabled()));
    }

    @GetMapping("/media/posts")
    @Operation(summary = "媒体管理（帖子图片，分页）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<MediaAssetResponse>> postMedia(
            @RequestParam(value = "communityId", required = false) Long communityId,
            @RequestParam(value = "postId", required = false) Long postId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminContentOpsService.pagePostMedia(communityId, postId, status, page, pageSize));
    }

    @PostMapping("/media/posts/{id}/offline")
    @Operation(summary = "下架媒体（帖子图片）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MediaAssetResponse> offlinePostMedia(@PathVariable("id") Long id) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(adminContentOpsService.offlinePostMedia(id, admin.userId()));
    }
}

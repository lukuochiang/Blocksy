package com.blocksy.server.modules.admin.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.community.dto.AdminCommunityCreateRequest;
import com.blocksy.server.modules.community.entity.CommunityEntity;
import com.blocksy.server.modules.community.service.CommunityService;
import com.blocksy.server.security.service.AdminGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/communities")
@Tag(name = "Admin-Community", description = "后台社区管理")
public class AdminCommunityController {

    private final CommunityService communityService;
    private final AdminGuard adminGuard;

    public AdminCommunityController(CommunityService communityService, AdminGuard adminGuard) {
        this.communityService = communityService;
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
}

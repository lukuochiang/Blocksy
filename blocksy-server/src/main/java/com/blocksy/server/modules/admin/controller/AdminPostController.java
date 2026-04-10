package com.blocksy.server.modules.admin.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.post.dto.AdminPostResponse;
import com.blocksy.server.modules.post.dto.AdminPostReviewRequest;
import com.blocksy.server.modules.post.service.PostService;
import com.blocksy.server.security.service.AdminGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/admin/posts")
@Tag(name = "Admin-Post", description = "后台帖子审核管理")
public class AdminPostController {

    private final PostService postService;
    private final AdminGuard adminGuard;

    public AdminPostController(PostService postService, AdminGuard adminGuard) {
        this.postService = postService;
        this.adminGuard = adminGuard;
    }

    @GetMapping
    @Operation(summary = "后台帖子列表")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<AdminPostResponse>> list(
            @Parameter(description = "帖子状态：1=正常，0=下架")
            @RequestParam(value = "status", required = false) Integer status,
            @Parameter(description = "社区 ID")
            @RequestParam(value = "communityId", required = false) Long communityId,
            @Parameter(description = "帖子内容关键词")
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "页码，从 1 开始")
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量")
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(postService.listForAdmin(status, communityId, keyword, page, pageSize));
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "后台审核帖子")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AdminPostResponse> review(@PathVariable("id") Long id, @Valid @RequestBody AdminPostReviewRequest request) {
        adminGuard.requireAdmin();
        return ApiResponse.success(postService.reviewForAdmin(id, request.action()));
    }
}

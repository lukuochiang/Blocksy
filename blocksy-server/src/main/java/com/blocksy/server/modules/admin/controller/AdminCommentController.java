package com.blocksy.server.modules.admin.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.comment.dto.AdminCommentResponse;
import com.blocksy.server.modules.comment.service.CommentService;
import com.blocksy.server.security.service.AdminGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/comments")
@Tag(name = "Admin-Comment", description = "后台评论管理")
public class AdminCommentController {

    private final CommentService commentService;
    private final AdminGuard adminGuard;

    public AdminCommentController(CommentService commentService, AdminGuard adminGuard) {
        this.commentService = commentService;
        this.adminGuard = adminGuard;
    }

    @GetMapping
    @Operation(summary = "后台评论列表")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<AdminCommentResponse>> list(
            @RequestParam(value = "postId", required = false) Long postId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(commentService.listForAdmin(postId, status, keyword));
    }

    @PostMapping("/{id}/delete")
    @Operation(summary = "后台删除评论")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        adminGuard.requireAdmin();
        commentService.deleteForAdmin(id);
        return ApiResponse.success();
    }
}

package com.blocksy.server.modules.comment.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.comment.dto.CommentRequest;
import com.blocksy.server.modules.comment.dto.CommentResponse;
import com.blocksy.server.modules.comment.service.CommentService;
import com.blocksy.server.security.SecurityUtils;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@Tag(name = "Comment", description = "帖子评论接口")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    @Operation(summary = "按帖子获取评论列表")
    public ApiResponse<List<CommentResponse>> list(@RequestParam("postId") Long postId) {
        return ApiResponse.success(commentService.listByPostId(postId));
    }

    @PostMapping
    @Operation(summary = "发布评论")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "发布成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误或帖子不存在")
    })
    public ApiResponse<CommentResponse> create(@Valid @RequestBody CommentRequest request) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(commentService.create(currentUser.userId(), request));
    }
}

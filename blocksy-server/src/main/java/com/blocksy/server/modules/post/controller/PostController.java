package com.blocksy.server.modules.post.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.post.dto.PostRequest;
import com.blocksy.server.modules.post.dto.PostResponse;
import com.blocksy.server.modules.post.service.PostService;
import com.blocksy.server.security.SecurityUtils;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Post", description = "邻里帖子接口")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    @Operation(summary = "获取帖子列表", description = "支持按 communityId 过滤")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ApiResponse<List<PostResponse>> list(@RequestParam(value = "communityId", required = false) Long communityId) {
        return ApiResponse.success(postService.list(communityId));
    }

    @GetMapping("/mine")
    @Operation(summary = "获取我的帖子列表", description = "支持按 communityId 过滤")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<PostResponse>> listMine(@RequestParam(value = "communityId", required = false) Long communityId) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(postService.listMine(currentUser.userId(), communityId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取帖子详情")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "帖子不存在")
    })
    public ApiResponse<PostResponse> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(postService.getById(id));
    }

    @PostMapping
    @Operation(summary = "发布帖子", description = "支持提交帖子正文和 media 图片数组")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "发布成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录")
    })
    public ApiResponse<PostResponse> create(@Valid @RequestBody PostRequest request) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(postService.create(currentUser.userId(), request));
    }
}

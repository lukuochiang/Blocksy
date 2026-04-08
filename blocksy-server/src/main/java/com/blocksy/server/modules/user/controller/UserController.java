package com.blocksy.server.modules.user.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.user.dto.SelectCommunityRequest;
import com.blocksy.server.modules.user.dto.UserCommunityItemResponse;
import com.blocksy.server.modules.user.entity.UserEntity;
import com.blocksy.server.modules.user.dto.UserMeResponse;
import com.blocksy.server.modules.user.service.UserService;
import com.blocksy.server.security.SecurityUtils;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "用户接口")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户信息")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录或 token 无效")
    })
    public ApiResponse<UserMeResponse> me() {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        UserEntity user = userService.findById(currentUser.userId());
        if (user == null) {
            throw new IllegalStateException("用户不存在");
        }
        Long defaultCommunityId = userService.getDefaultCommunityId(user.getId());
        return ApiResponse.success(new UserMeResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getStatus(),
                defaultCommunityId
        ));
    }

    @GetMapping("/communities")
    @Operation(summary = "获取当前用户可选社区")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<UserCommunityItemResponse>> myCommunities() {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(userService.listUserCommunities(currentUser.userId()));
    }

    @PutMapping("/community")
    @Operation(summary = "切换当前用户默认社区")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> selectCommunity(@Valid @RequestBody SelectCommunityRequest request) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        userService.selectDefaultCommunity(currentUser.userId(), request.communityId());
        return ApiResponse.success();
    }
}

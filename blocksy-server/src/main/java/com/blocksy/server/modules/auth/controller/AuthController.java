package com.blocksy.server.modules.auth.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.auth.dto.LoginRequest;
import com.blocksy.server.modules.auth.dto.LoginResponse;
import com.blocksy.server.modules.auth.service.AuthService;
import com.blocksy.server.modules.user.entity.UserEntity;
import com.blocksy.server.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "认证接口")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    public AuthController(JwtTokenProvider jwtTokenProvider, AuthService authService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回 JWT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登录成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "用户名或密码错误")
    })
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        UserEntity user = authService.login(request);
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        return ApiResponse.success(new LoginResponse(user.getId(), user.getUsername(), token));
    }
}

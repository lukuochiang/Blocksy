package com.blocksy.server.modules.auth.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.admin.entity.UserBehaviorLogEntity;
import com.blocksy.server.modules.admin.mapper.UserBehaviorLogMapper;
import com.blocksy.server.modules.auth.dto.LoginRequest;
import com.blocksy.server.modules.auth.dto.LoginResponse;
import com.blocksy.server.modules.auth.service.AuthService;
import com.blocksy.server.modules.user.entity.UserEntity;
import com.blocksy.server.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "认证接口")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final UserBehaviorLogMapper userBehaviorLogMapper;

    public AuthController(
            JwtTokenProvider jwtTokenProvider,
            AuthService authService,
            UserBehaviorLogMapper userBehaviorLogMapper
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authService = authService;
        this.userBehaviorLogMapper = userBehaviorLogMapper;
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回 JWT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登录成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "用户名或密码错误")
    })
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        UserEntity user = authService.login(request);
        appendLoginBehavior(user.getId(), httpRequest);
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        return ApiResponse.success(new LoginResponse(user.getId(), user.getUsername(), token));
    }

    private void appendLoginBehavior(Long userId, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        UserBehaviorLogEntity log = new UserBehaviorLogEntity();
        log.setUserId(userId);
        log.setBehaviorType("LOGIN");
        log.setResourceType("AUTH");
        log.setResourceId(null);
        log.setIp(resolveIp(request));
        log.setDevice(request.getHeader("User-Agent"));
        log.setStatus(1);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        userBehaviorLogMapper.insert(log);
    }

    private String resolveIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return comma > 0 ? xff.substring(0, comma).trim() : xff.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}

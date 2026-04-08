package com.blocksy.server.modules.auth.service;

import com.blocksy.server.modules.auth.dto.LoginRequest;
import com.blocksy.server.modules.user.entity.UserEntity;
import com.blocksy.server.modules.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity login(LoginRequest request) {
        UserEntity user = userService.findByUsername(request.username());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return user;
    }
}

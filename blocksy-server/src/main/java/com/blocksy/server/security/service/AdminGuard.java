package com.blocksy.server.security.service;

import com.blocksy.server.common.enums.ResponseCodeEnum;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.security.SecurityUtils;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AdminGuard {

    private final Set<String> adminUsernames;

    public AdminGuard(@Value("${blocksy.security.admin-usernames:demo}") String adminUsernames) {
        this.adminUsernames = Arrays.stream(adminUsernames.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toSet());
    }

    public AuthenticatedUser requireAdmin() {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        if (!adminUsernames.contains(currentUser.username())) {
            throw new BusinessException(ResponseCodeEnum.FORBIDDEN, "仅管理员可操作");
        }
        return currentUser;
    }
}

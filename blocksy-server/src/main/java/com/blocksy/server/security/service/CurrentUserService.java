package com.blocksy.server.security.service;

import com.blocksy.server.security.SecurityUtils;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    public AuthenticatedUser getCurrentUser() {
        return SecurityUtils.getCurrentUserOrThrow();
    }
}

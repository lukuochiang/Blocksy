package com.blocksy.server.security.jwt;

public record AuthenticatedUser(Long userId, String username) {
}

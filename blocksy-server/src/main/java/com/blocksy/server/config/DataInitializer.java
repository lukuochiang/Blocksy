package com.blocksy.server.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        Long communityId = ensureDefaultCommunity();
        Long userId = ensureDemoUser();
        ensureUserCommunity(userId, communityId);
    }

    private Long ensureDefaultCommunity() {
        Long communityId = jdbcTemplate.query(
                        "SELECT id FROM communities WHERE code = ? LIMIT 1",
                        (rs, rowNum) -> rs.getLong("id"),
                        "DEFAULT-COMMUNITY"
                )
                .stream()
                .findFirst()
                .orElse(null);
        if (communityId != null) {
            return communityId;
        }
        jdbcTemplate.update(
                "INSERT INTO communities(code, name, address, description, status) VALUES (?, ?, ?, ?, ?)",
                "DEFAULT-COMMUNITY",
                "Norvo 默认社区",
                "本地开发地址",
                "用于本地 MVP 调试",
                1
        );
        return jdbcTemplate.query(
                        "SELECT id FROM communities WHERE code = ? LIMIT 1",
                        (rs, rowNum) -> rs.getLong("id"),
                        "DEFAULT-COMMUNITY"
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("初始化默认社区失败"));
    }

    private Long ensureDemoUser() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM users WHERE username = ?",
                Integer.class,
                "demo"
        );
        if (count == null || count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO users(username, password_hash, nickname, status) VALUES (?, ?, ?, ?)",
                    "demo",
                    passwordEncoder.encode("blocksy123"),
                    "Norvo Demo",
                    1
            );
        }
        Long userId = jdbcTemplate.query(
                        "SELECT id FROM users WHERE username = ? LIMIT 1",
                        (rs, rowNum) -> rs.getLong("id"),
                        "demo"
                )
                .stream()
                .findFirst()
                .orElse(null);
        if (userId == null) {
            throw new IllegalStateException("初始化 demo 用户失败");
        }
        return userId;
    }

    private void ensureUserCommunity(Long userId, Long communityId) {
        Integer relationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM user_community WHERE user_id = ? AND community_id = ?",
                Integer.class,
                userId,
                communityId
        );
        if (relationCount == null || relationCount == 0) {
            jdbcTemplate.update(
                    "INSERT INTO user_community(user_id, community_id, is_default, status) VALUES (?, ?, ?, ?)",
                    userId,
                    communityId,
                    true,
                    1
            );
        }
    }
}

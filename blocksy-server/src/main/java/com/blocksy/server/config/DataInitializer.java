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
        ensureOperationNoteTemplates();
        ensureRiskModuleSeeds(userId);
        ensureMenuPermissionSeeds();
        ensureDataPermissionSeeds();
        ensureUserBehaviorSeeds(userId);
        ensureAdminOperationLogSeeds(userId);
        ensureVerificationSeeds(userId);
        ensureCommunityNoticeSeeds(userId, communityId);
        ensureEventSignupSeeds(userId, communityId);
        ensureContentCategorySeeds();
        ensureNotificationTemplateSeeds();
        ensurePlatformSettingSeeds();
        ensurePolicyDocumentSeeds(userId);
        ensurePushTaskSeeds(userId);
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

    private void ensureOperationNoteTemplates() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM operation_note_templates WHERE module = ?",
                Integer.class,
                "EVENT"
        );
        if (count != null && count > 0) {
            return;
        }
        insertTemplate("EVENT", "OFFLINE", "违规活动内容，已下架", 10);
        insertTemplate("EVENT", "OFFLINE", "活动信息不完整，暂时下架", 20);
        insertTemplate("EVENT", "RESTORE", "复核通过，恢复展示", 30);
        insertTemplate("EVENT", "DELETE", "重复活动，已清理", 40);
    }

    private void insertTemplate(String module, String action, String content, int sortNo) {
        jdbcTemplate.update(
                "INSERT INTO operation_note_templates(module, action, content, sort_no, status) VALUES (?, ?, ?, ?, ?)",
                module,
                action,
                content,
                sortNo,
                1
        );
    }

    private void ensureRiskModuleSeeds(Long userId) {
        if (!tableExists("risk_anomalies") || !tableExists("risk_appeals")) {
            return;
        }
        Integer anomalyCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM risk_anomalies", Integer.class);
        if (anomalyCount == null || anomalyCount == 0) {
            jdbcTemplate.update(
                    "INSERT INTO risk_anomalies(user_id, anomaly_type, level, details, process_status, status) VALUES (?, ?, ?, ?, ?, ?)",
                    userId,
                    "SPAM_POSTING",
                    "HIGH",
                    "近1小时发帖次数异常升高",
                    "PENDING",
                    1
            );
            jdbcTemplate.update(
                    "INSERT INTO risk_anomalies(user_id, anomaly_type, level, details, process_status, status) VALUES (?, ?, ?, ?, ?, ?)",
                    userId,
                    "ABNORMAL_LOGIN",
                    "MEDIUM",
                    "设备和IP在短时间内频繁切换",
                    "PENDING",
                    1
            );
        }
        Integer appealCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM risk_appeals", Integer.class);
        if (appealCount == null || appealCount == 0) {
            jdbcTemplate.update(
                    "INSERT INTO risk_appeals(user_id, appeal_reason, appeal_content, process_status, status) VALUES (?, ?, ?, ?, ?)",
                    userId,
                    "误封申诉",
                    "我认为处罚存在误判，请复核最近发帖内容。",
                    "PENDING",
                    1
            );
        }
    }

    private void ensureMenuPermissionSeeds() {
        ensureMenuPermission("ADMIN", "dashboard", "仪表盘", "/dashboard");
        ensureMenuPermission("ADMIN", "risk.anomaly", "异常行为监控", "/risk/anomaly");
        ensureMenuPermission("ADMIN", "risk.appeals", "申诉处理", "/risk/appeals");
        ensureMenuPermission("ADMIN", "permissions.menus", "菜单权限", "/permissions/menus");
    }

    private void ensureMenuPermission(String roleCode, String menuKey, String menuName, String menuPath) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM menu_permissions WHERE role_code = ? AND menu_key = ?",
                Integer.class,
                roleCode,
                menuKey
        );
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO menu_permissions(role_code, menu_key, menu_name, menu_path, enabled, status) VALUES (?, ?, ?, ?, ?, ?)",
                roleCode,
                menuKey,
                menuName,
                menuPath,
                true,
                1
        );
    }

    private void ensureDataPermissionSeeds() {
        ensureDataPermission("ADMIN", "COMMUNITY", "1");
        ensureDataPermission("ADMIN", "COMMUNITY", "2");
        ensureDataPermission("OPERATOR", "COMMUNITY", "1");
    }

    private void ensureDataPermission(String roleCode, String dataScope, String dataValue) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM data_permissions WHERE role_code = ? AND data_scope = ? AND data_value = ?",
                Integer.class,
                roleCode,
                dataScope,
                dataValue
        );
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO data_permissions(role_code, data_scope, data_value, enabled, status) VALUES (?, ?, ?, ?, ?)",
                roleCode,
                dataScope,
                dataValue,
                true,
                1
        );
    }

    private void ensureUserBehaviorSeeds(Long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM user_behavior_logs", Integer.class);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO user_behavior_logs(user_id, behavior_type, resource_type, resource_id, ip, device, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
                userId,
                "LOGIN",
                "AUTH",
                null,
                "127.0.0.1",
                "web-h5",
                1
        );
        jdbcTemplate.update(
                "INSERT INTO user_behavior_logs(user_id, behavior_type, resource_type, resource_id, ip, device, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
                userId,
                "CREATE_POST",
                "POST",
                1L,
                "127.0.0.1",
                "web-h5",
                1
        );
    }

    private void ensureAdminOperationLogSeeds(Long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM admin_operation_logs", Integer.class);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO admin_operation_logs(module, action, operator_user_id, target_type, target_id, details, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
                "PERMISSION",
                "INIT_SEED",
                userId,
                "SYSTEM",
                null,
                "初始化权限相关种子数据",
                1
        );
    }

    private void ensureVerificationSeeds(Long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM user_verification_applications", Integer.class);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO user_verification_applications(user_id, verify_type, real_name, id_card_mask, material_urls, process_status, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
                userId,
                "RESIDENT",
                "Demo User",
                "310***********1234",
                "http://localhost/mock/material-1.jpg",
                "PENDING",
                1
        );
    }

    private void ensureCommunityNoticeSeeds(Long userId, Long communityId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM community_notices", Integer.class);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO community_notices(community_id, title, content, status, created_by) VALUES (?, ?, ?, ?, ?)",
                communityId,
                "小区电梯保养通知",
                "本周六 10:00-12:00 进行电梯例行保养，请居民提前安排出行。",
                1,
                userId
        );
    }

    private void ensureEventSignupSeeds(Long userId, Long communityId) {
        Integer eventCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM events", Integer.class);
        Long eventId;
        if (eventCount == null || eventCount == 0) {
            jdbcTemplate.update(
                    "INSERT INTO events(user_id, community_id, title, content, location, start_time, end_time, signup_limit, signup_count, status) VALUES (?, ?, ?, ?, ?, NOW() + INTERVAL '1 day', NOW() + INTERVAL '2 day', ?, ?, ?)",
                    userId,
                    communityId,
                    "社区清洁日",
                    "周末一起清理公共区域，欢迎报名参加。",
                    "中心花园",
                    20,
                    0,
                    1
            );
        }
        eventId = jdbcTemplate.query(
                        "SELECT id FROM events ORDER BY id DESC LIMIT 1",
                        (rs, rowNum) -> rs.getLong("id")
                )
                .stream()
                .findFirst()
                .orElse(null);
        if (eventId == null) {
            return;
        }
        Integer signupCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM event_signups WHERE event_id = ? AND user_id = ?",
                Integer.class,
                eventId,
                userId
        );
        if (signupCount == null || signupCount == 0) {
            jdbcTemplate.update(
                    "INSERT INTO event_signups(event_id, user_id, remark, status) VALUES (?, ?, ?, ?)",
                    eventId,
                    userId,
                    "默认报名记录",
                    1
            );
            jdbcTemplate.update("UPDATE events SET signup_count = signup_count + 1 WHERE id = ?", eventId);
        }
    }

    private void ensureContentCategorySeeds() {
        ensureContentCategory("LISTING", "SECOND_HAND", "二手交易", 10);
        ensureContentCategory("LISTING", "LOST_FOUND", "失物招领", 20);
        ensureContentCategory("LISTING", "HELP_BUY", "求助求购", 30);
        ensureContentCategory("POST", "NEIGHBOR_CHAT", "邻里闲聊", 10);
        ensureContentCategory("POST", "COMMUNITY_NEWS", "社区资讯", 20);
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private void ensureContentCategory(String module, String code, String name, int sortNo) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM content_categories WHERE module = ? AND code = ?",
                Integer.class,
                module,
                code
        );
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO content_categories(module, code, name, sort_no, enabled, status) VALUES (?, ?, ?, ?, ?, ?)",
                module,
                code,
                name,
                sortNo,
                true,
                1
        );
    }

    private void ensureNotificationTemplateSeeds() {
        ensureNotificationTemplate("REPORT", "REPORT_HANDLED", "举报处理结果通知", "你提交的举报已处理，处理结果：{{result}}。", true);
        ensureNotificationTemplate("EVENT", "EVENT_SIGNUP", "活动报名通知", "你已成功报名活动《{{eventTitle}}》。", true);
        ensureNotificationTemplate("SYSTEM", "ANNOUNCEMENT", "系统公告通知", "{{content}}", true);
    }

    private void ensureNotificationTemplate(String module, String triggerCode, String titleTemplate, String contentTemplate, boolean enabled) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM notification_templates WHERE module = ? AND trigger_code = ?",
                Integer.class,
                module,
                triggerCode
        );
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO notification_templates(module, trigger_code, title_template, content_template, enabled, status) VALUES (?, ?, ?, ?, ?, ?)",
                module,
                triggerCode,
                titleTemplate,
                contentTemplate,
                enabled,
                1
        );
    }

    private void ensurePlatformSettingSeeds() {
        ensurePlatformSetting("BASIC", "site_name", "Norvo 社区");
        ensurePlatformSetting("UPLOAD", "max_file_size", "20MB");
        ensurePlatformSetting("UPLOAD", "allowed_types", "jpg,png,webp,mp4");
        ensurePlatformSetting("INTEGRATIONS", "sms_provider", "mock-sms");
        ensurePlatformSetting("BRAND", "logo_url", "http://localhost:9000/blocksy-media/system/logo.png");
        ensurePlatformSetting("ANALYTICS", "default_window_days", "7");
        ensurePlatformSetting("ANALYTICS", "max_window_days", "90");
        ensurePlatformSetting("ANALYTICS", "allowed_window_days", "7,14,30,90");
    }

    private void ensurePlatformSetting(String settingGroup, String settingKey, String settingValue) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM platform_settings WHERE setting_group = ? AND setting_key = ?",
                Integer.class,
                settingGroup,
                settingKey
        );
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO platform_settings(setting_group, setting_key, setting_value, description, status) VALUES (?, ?, ?, ?, ?)",
                settingGroup,
                settingKey,
                settingValue,
                "seed",
                1
        );
    }

    private void ensurePolicyDocumentSeeds(Long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM policy_documents", Integer.class);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO policy_documents(policy_type, version, title, content, is_active, created_by, published_at, status) VALUES (?, ?, ?, ?, ?, ?, NOW(), ?)",
                "USER_AGREEMENT",
                "v1.0.0",
                "用户协议",
                "这是 Blocksy MVP 用户协议示例内容。",
                true,
                userId,
                1
        );
        jdbcTemplate.update(
                "INSERT INTO policy_documents(policy_type, version, title, content, is_active, created_by, published_at, status) VALUES (?, ?, ?, ?, ?, ?, NOW(), ?)",
                "PRIVACY_POLICY",
                "v1.0.0",
                "隐私政策",
                "这是 Blocksy MVP 隐私政策示例内容。",
                true,
                userId,
                1
        );
    }

    private void ensurePushTaskSeeds(Long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM push_tasks", Integer.class);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO push_tasks(title, content, target_type, task_status, created_by, status) VALUES (?, ?, ?, ?, ?, ?)",
                "欢迎使用 Blocksy",
                "欢迎加入本地社区，请完善个人信息并选择社区。",
                "ALL",
                "PENDING",
                userId,
                1
        );
    }
}

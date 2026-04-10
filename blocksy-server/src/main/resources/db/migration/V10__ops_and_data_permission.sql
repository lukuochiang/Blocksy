CREATE TABLE IF NOT EXISTS user_behavior_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    behavior_type VARCHAR(64) NOT NULL,
    resource_type VARCHAR(64),
    resource_id BIGINT,
    ip VARCHAR(64),
    device VARCHAR(128),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_user_behavior_logs_user_id ON user_behavior_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_user_behavior_logs_behavior_type ON user_behavior_logs(behavior_type);
CREATE INDEX IF NOT EXISTS idx_user_behavior_logs_created_at ON user_behavior_logs(created_at DESC);

CREATE TABLE IF NOT EXISTS data_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_code VARCHAR(64) NOT NULL,
    data_scope VARCHAR(64) NOT NULL,
    data_value VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_data_permissions_role_scope_value ON data_permissions(role_code, data_scope, data_value);
CREATE INDEX IF NOT EXISTS idx_data_permissions_role_code ON data_permissions(role_code);

CREATE TABLE IF NOT EXISTS admin_operation_logs (
    id BIGSERIAL PRIMARY KEY,
    module VARCHAR(64) NOT NULL,
    action VARCHAR(64) NOT NULL,
    operator_user_id BIGINT NOT NULL,
    target_type VARCHAR(64),
    target_id BIGINT,
    details VARCHAR(500),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_admin_operation_logs_module_action ON admin_operation_logs(module, action);
CREATE INDEX IF NOT EXISTS idx_admin_operation_logs_created_at ON admin_operation_logs(created_at DESC);

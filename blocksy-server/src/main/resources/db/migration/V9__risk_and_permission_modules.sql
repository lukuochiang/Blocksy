CREATE TABLE IF NOT EXISTS risk_anomalies (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    anomaly_type VARCHAR(64) NOT NULL,
    level VARCHAR(16) NOT NULL DEFAULT 'MEDIUM',
    details VARCHAR(500),
    process_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    handle_note VARCHAR(255),
    assignee_user_id BIGINT,
    processed_at TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_risk_anomalies_process_status ON risk_anomalies(process_status);
CREATE INDEX IF NOT EXISTS idx_risk_anomalies_user_id ON risk_anomalies(user_id);

CREATE TABLE IF NOT EXISTS risk_appeals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    punish_log_id BIGINT,
    appeal_reason VARCHAR(255),
    appeal_content VARCHAR(500) NOT NULL,
    process_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    result_note VARCHAR(255),
    assignee_user_id BIGINT,
    processed_at TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_risk_appeals_process_status ON risk_appeals(process_status);
CREATE INDEX IF NOT EXISTS idx_risk_appeals_user_id ON risk_appeals(user_id);

CREATE TABLE IF NOT EXISTS menu_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_code VARCHAR(64) NOT NULL,
    menu_key VARCHAR(128) NOT NULL,
    menu_name VARCHAR(120) NOT NULL,
    menu_path VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_menu_permissions_role_menu ON menu_permissions(role_code, menu_key);
CREATE INDEX IF NOT EXISTS idx_menu_permissions_role_code ON menu_permissions(role_code);

CREATE TABLE IF NOT EXISTS permission_op_logs (
    id BIGSERIAL PRIMARY KEY,
    role_code VARCHAR(64) NOT NULL,
    operator_user_id BIGINT NOT NULL,
    action VARCHAR(64) NOT NULL,
    details VARCHAR(500),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_permission_op_logs_role_code ON permission_op_logs(role_code);

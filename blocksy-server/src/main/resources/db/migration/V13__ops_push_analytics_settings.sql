CREATE TABLE IF NOT EXISTS push_tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    content VARCHAR(500) NOT NULL,
    target_type VARCHAR(32) NOT NULL DEFAULT 'ALL',
    task_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    created_by BIGINT,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_push_tasks_status_created_at ON push_tasks(task_status, created_at DESC);

CREATE TABLE IF NOT EXISTS push_records (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT,
    channel VARCHAR(32) NOT NULL DEFAULT 'IN_APP',
    send_status VARCHAR(32) NOT NULL DEFAULT 'SENT',
    read_status BOOLEAN NOT NULL DEFAULT FALSE,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_push_records_task_id ON push_records(task_id);
CREATE INDEX IF NOT EXISTS idx_push_records_send_status ON push_records(send_status);

CREATE TABLE IF NOT EXISTS notification_templates (
    id BIGSERIAL PRIMARY KEY,
    module VARCHAR(64) NOT NULL,
    trigger_code VARCHAR(64) NOT NULL,
    title_template VARCHAR(120) NOT NULL,
    content_template VARCHAR(500) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_notification_templates_module_trigger ON notification_templates(module, trigger_code);

CREATE TABLE IF NOT EXISTS platform_settings (
    id BIGSERIAL PRIMARY KEY,
    setting_group VARCHAR(64) NOT NULL,
    setting_key VARCHAR(128) NOT NULL,
    setting_value VARCHAR(2000),
    description VARCHAR(255),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_platform_settings_group_key ON platform_settings(setting_group, setting_key);

CREATE TABLE IF NOT EXISTS policy_documents (
    id BIGSERIAL PRIMARY KEY,
    policy_type VARCHAR(64) NOT NULL,
    version VARCHAR(32) NOT NULL,
    title VARCHAR(120) NOT NULL,
    content TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    status SMALLINT NOT NULL DEFAULT 1,
    created_by BIGINT,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_policy_documents_type_active ON policy_documents(policy_type, is_active);

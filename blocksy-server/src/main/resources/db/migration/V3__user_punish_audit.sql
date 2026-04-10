ALTER TABLE users
    ADD COLUMN IF NOT EXISTS ban_reason VARCHAR(500),
    ADD COLUMN IF NOT EXISTS banned_by BIGINT,
    ADD COLUMN IF NOT EXISTS banned_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS banned_until TIMESTAMP;

CREATE TABLE IF NOT EXISTS user_punish_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    operator_user_id BIGINT,
    action VARCHAR(32) NOT NULL,
    reason VARCHAR(500),
    duration_hours INT,
    expires_at TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_punish_logs_user_id ON user_punish_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_user_punish_logs_operator_user_id ON user_punish_logs(operator_user_id);
CREATE INDEX IF NOT EXISTS idx_user_punish_logs_created_at ON user_punish_logs(created_at DESC);

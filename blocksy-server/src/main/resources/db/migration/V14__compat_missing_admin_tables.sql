-- Compatibility patch for legacy databases missing risk/admin tables.

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

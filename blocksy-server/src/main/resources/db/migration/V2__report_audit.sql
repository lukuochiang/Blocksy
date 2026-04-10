ALTER TABLE reports
    ADD COLUMN IF NOT EXISTS handler_note VARCHAR(500);

CREATE TABLE IF NOT EXISTS report_handle_logs (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL,
    operator_user_id BIGINT NOT NULL,
    action VARCHAR(32) NOT NULL,
    note VARCHAR(500),
    ban_target_user BOOLEAN NOT NULL DEFAULT FALSE,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_report_handle_logs_report_id ON report_handle_logs(report_id);
CREATE INDEX IF NOT EXISTS idx_report_handle_logs_operator_user_id ON report_handle_logs(operator_user_id);
CREATE INDEX IF NOT EXISTS idx_report_handle_logs_created_at ON report_handle_logs(created_at DESC);

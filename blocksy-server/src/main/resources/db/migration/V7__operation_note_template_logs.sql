CREATE TABLE IF NOT EXISTS operation_note_template_logs (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    operator_user_id BIGINT NOT NULL,
    action VARCHAR(32) NOT NULL,
    note VARCHAR(500),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_operation_note_template_logs_template_id
    ON operation_note_template_logs(template_id);
CREATE INDEX IF NOT EXISTS idx_operation_note_template_logs_operator_user_id
    ON operation_note_template_logs(operator_user_id);
CREATE INDEX IF NOT EXISTS idx_operation_note_template_logs_created_at
    ON operation_note_template_logs(created_at DESC);

CREATE TABLE IF NOT EXISTS event_handle_logs (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    operator_user_id BIGINT NOT NULL,
    action VARCHAR(32) NOT NULL,
    note VARCHAR(500),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_event_handle_logs_event_id ON event_handle_logs(event_id);
CREATE INDEX IF NOT EXISTS idx_event_handle_logs_operator_user_id ON event_handle_logs(operator_user_id);
CREATE INDEX IF NOT EXISTS idx_event_handle_logs_created_at ON event_handle_logs(created_at DESC);

CREATE TABLE IF NOT EXISTS listing_handle_logs (
    id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    operator_user_id BIGINT NOT NULL,
    action VARCHAR(32) NOT NULL,
    note VARCHAR(500),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_listing_handle_logs_listing_id ON listing_handle_logs(listing_id);
CREATE INDEX IF NOT EXISTS idx_listing_handle_logs_operator_user_id ON listing_handle_logs(operator_user_id);
CREATE INDEX IF NOT EXISTS idx_listing_handle_logs_created_at ON listing_handle_logs(created_at DESC);

CREATE TABLE IF NOT EXISTS system_announcements (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    content VARCHAR(500) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    created_by BIGINT,
    revoked_by BIGINT,
    revoked_at TIMESTAMP,
    dispatch_count INT NOT NULL DEFAULT 0,
    last_dispatched_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_system_announcements_status_created_at
    ON system_announcements(status, created_at DESC);

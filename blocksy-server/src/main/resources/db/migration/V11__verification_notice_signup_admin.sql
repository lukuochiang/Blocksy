CREATE TABLE IF NOT EXISTS user_verification_applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    verify_type VARCHAR(32) NOT NULL DEFAULT 'RESIDENT',
    real_name VARCHAR(64),
    id_card_mask VARCHAR(64),
    material_urls VARCHAR(1000),
    process_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    review_note VARCHAR(255),
    reviewer_user_id BIGINT,
    reviewed_at TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_user_verification_process_status ON user_verification_applications(process_status);
CREATE INDEX IF NOT EXISTS idx_user_verification_user_id ON user_verification_applications(user_id);

CREATE TABLE IF NOT EXISTS community_notices (
    id BIGSERIAL PRIMARY KEY,
    community_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    created_by BIGINT,
    revoked_by BIGINT,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_community_notices_community_id ON community_notices(community_id);
CREATE INDEX IF NOT EXISTS idx_community_notices_status_created_at ON community_notices(status, created_at DESC);

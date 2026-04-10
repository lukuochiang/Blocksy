CREATE TABLE IF NOT EXISTS content_categories (
    id BIGSERIAL PRIMARY KEY,
    module VARCHAR(32) NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(120) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_content_categories_module_code ON content_categories(module, code);
CREATE INDEX IF NOT EXISTS idx_content_categories_module_sort ON content_categories(module, sort_no);

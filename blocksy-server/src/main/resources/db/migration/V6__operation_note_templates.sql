CREATE TABLE IF NOT EXISTS operation_note_templates (
    id BIGSERIAL PRIMARY KEY,
    module VARCHAR(32) NOT NULL,
    action VARCHAR(32) NOT NULL,
    content VARCHAR(255) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_operation_note_templates_module_action
    ON operation_note_templates(module, action);
CREATE INDEX IF NOT EXISTS idx_operation_note_templates_status_sort
    ON operation_note_templates(status, sort_no, created_at DESC);

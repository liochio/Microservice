-- ==============================================================================
-- Migration V4: Bổ sung Idempotency, In-App Read Status và Template Tracking
-- ==============================================================================

ALTER TABLE notifications ADD COLUMN idempotency_key VARCHAR(100) NULL;
ALTER TABLE notifications ADD COLUMN template_code VARCHAR(100) NULL;
ALTER TABLE notifications ADD COLUMN is_read BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE notifications ADD COLUMN read_at TIMESTAMP NULL;

CREATE UNIQUE INDEX uk_noti_idempotency ON notifications (idempotency_key);
CREATE INDEX idx_noti_recipient_read ON notifications (recipient, is_read);

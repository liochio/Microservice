-- ==============================================================================
-- Migration V2: Bổ sung Transaction-Bound OTP Context & IP Lockdown Tracking
-- ==============================================================================

ALTER TABLE user_otp_verifications
    ADD COLUMN IF NOT EXISTS 	x_context_hash VARCHAR(128) NULL,
    ADD COLUMN IF NOT EXISTS client_ip VARCHAR(50) NULL;

CREATE INDEX IF NOT EXISTS idx_otp_dest_created ON user_otp_verifications (	arget_destination, created_at);
CREATE INDEX IF NOT EXISTS idx_otp_user_created ON user_otp_verifications (user_id, created_at);
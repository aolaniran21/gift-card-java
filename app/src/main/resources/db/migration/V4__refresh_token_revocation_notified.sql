-- Add a flag to mark that a revoked token reuse has already been observed
ALTER TABLE refresh_tokens ADD COLUMN revocation_notified BOOLEAN DEFAULT FALSE NOT NULL;

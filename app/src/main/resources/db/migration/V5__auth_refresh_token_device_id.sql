-- Add device_id to refresh_tokens for device binding
ALTER TABLE refresh_tokens ADD COLUMN device_id VARCHAR(255);

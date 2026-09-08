-- Add created_at column to refresh_tokens and backfill with current timestamp for existing rows
ALTER TABLE refresh_tokens ADD COLUMN created_at TIMESTAMP;
-- Set created_at for existing rows to now() so older rows have a value
UPDATE refresh_tokens SET created_at = NOW() WHERE created_at IS NULL;
-- Note: The application will set created_at for new tokens when saving them.

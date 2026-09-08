-- Migrate existing refresh tokens to a LEGACY sentinel and enforce NOT NULL on device_id
-- NOTE FOR DEVELOPERS: the sentinel value used is defined in code as
-- com.example.auth.infrastructure.security.RefreshTokenConstants.LEGACY_DEVICE_ID
-- The SQL below sets that sentinel for existing rows and as the default for new rows.
UPDATE refresh_tokens SET device_id = '__LEGACY__' WHERE device_id IS NULL;
ALTER TABLE refresh_tokens ALTER COLUMN device_id SET DEFAULT '__LEGACY__';
ALTER TABLE refresh_tokens ALTER COLUMN device_id SET NOT NULL;

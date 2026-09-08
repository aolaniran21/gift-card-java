Legacy refresh token sentinel and rollout

Purpose

This short developer note documents the special "legacy" sentinel used for pre-device-binding refresh tokens, where it appears in code and database migrations, and recommended rollout steps for making device binding strict.

Key constants and locations

- Code constant: com.example.auth.infrastructure.security.RefreshTokenConstants.LEGACY_DEVICE_ID
  - Value: "__LEGACY__"
  - File: auth/src/main/java/com/example/auth/infrastructure/security/RefreshTokenConstants.java

- Database migration (sets existing NULL device_id -> sentinel and makes column NOT NULL):
  - File: app/src/main/resources/db/migration/V6__auth_enforce_device_id_not_null.sql
  - Contains SQL statements (these must use the literal '__LEGACY__' so Flyway can execute them):
    - UPDATE refresh_tokens SET device_id = '__LEGACY__' WHERE device_id IS NULL;
    - ALTER TABLE refresh_tokens ALTER COLUMN device_id SET DEFAULT '__LEGACY__';
    - ALTER TABLE refresh_tokens ALTER COLUMN device_id SET NOT NULL;

- Verification logic:
  - RefreshTokenRepositoryAdapter uses the stored device_id when verifying the HMAC payload; if the stored device_id equals the sentinel (RefreshTokenConstants.LEGACY_DEVICE_ID) the adapter accepts the presented device id from the client for compatibility.
  - File: auth/src/main/java/com/example/auth/infrastructure/persistence/RefreshTokenRepositoryAdapter.java

Why both code constant and SQL literal exist

- The SQL migration must contain the literal '__LEGACY__' because Flyway runs SQL against the database; it cannot refer to Java constants.
- The code should use the centralized constant RefreshTokenConstants.LEGACY_DEVICE_ID to avoid typos and make any future changes simple.
- To keep the database and code consistent, update the migration comments to reference the code constant (done) and change the constant value only with caution (see rollout steps below).

Rollout guidance (recommended)

1. Deploy the code that introduces device binding and the refresh token behavior (this code expects X-Device-Id for new tokens):
   - Ensure production configuration contains a secure HMAC secret for refresh token signing (configured via refresh.token.hmac-secret property).

2. Run V5 migration (adds device_id column, nullable) and V6 migration (sets device_id='__LEGACY__' for existing rows and makes column NOT NULL).
   - Because V6 writes the literal '__LEGACY__' into the DB, code that compares against RefreshTokenConstants.LEGACY_DEVICE_ID will match existing rows.

3. Compatibility window (operate for a controlled period):
   - Leave the compatibility behavior in place (adapter accepts presented device for legacy tokens) while clients are upgraded to send X-Device-Id and obtain new device-bound tokens.
   - Monitor token rotation metrics and login/refresh traffic to measure how quickly legacy tokens are being rotated out.

   Example curl requests (new device-bound token):

   - Obtain a refresh token (login) — include X-Device-Id header:

     curl -i -X POST 'https://api.example.com/api/auth/login' \
       -H 'Content-Type: application/json' \
       -H 'X-Device-Id: device-abc-123' \
       -d '{"email":"user@example.com","password":"secret"}'

   - Use refresh endpoint (include the header):

     curl -i -X POST 'https://api.example.com/api/auth/refresh' \
       -H 'Authorization: Bearer <access-token>' \
       -H 'X-Device-Id: device-abc-123' \
       -d '{"refreshToken":"<refresh-token>"}'

   Example for legacy tokens (behavior):

   - Tokens created before device binding are persisted with device_id='__LEGACY__' in the DB. The server will accept any X-Device-Id when verifying those legacy tokens to preserve compatibility until they are rotated or revoked.

4. Expire legacy tokens after the compatibility window:
   - After a chosen TTL (e.g., 30 days), run a one-off maintenance job to revoke or delete tokens where device_id = '__LEGACY__'.
   - Alternatively, set a stricter max_expires for legacy tokens and let them naturally expire.

5. Optional hardening: migrate away from a sentinel literal

- If you prefer not to use a sentinel string in the DB long-term, you can:
  - Add a separate boolean column (is_legacy) and migrate rows to set that flag during V6; the code would then check that flag instead of a string sentinel.
  - Or create a small enum-type or FK to a device table.
- These changes require additional migrations and a short compatibility period.

Tests and verification

- Unit tests already use RefreshTokenConstants.LEGACY_DEVICE_ID when constructing legacy-style tokens for integration tests:
  - app/src/test/java/com/example/modularmonolith/AuthIntegrationTest.java
- When changing the sentinel value, update the migration SQL (if appropriate), the constant value, and all tests that assume the value.

Checklist for changing the sentinel in future

1. Add a migration that writes the new sentinel into existing rows (if you must change the stored value).
2. Update RefreshTokenConstants.LEGACY_DEVICE_ID in code.
3. Update tests that assert on the literal value or compute HMACs using it.
4. Run full test suite (including integration tests against Postgres Testcontainers in CI) before deploying.

Contact

If you need help implementing an expirer job or migrating the sentinel to a different DB representation, ask and a follow-up implementation can be prepared.

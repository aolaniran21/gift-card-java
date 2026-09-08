-- Flyway V2: Phase 2 core tables: wallets, transactions, ledger_entries, audit_log, idempotency_keys, outbox

CREATE TABLE wallets (
  id BIGSERIAL PRIMARY KEY,
  owner_id BIGINT NOT NULL,
  currency VARCHAR(8) NOT NULL,
  metadata TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE transactions (
  id BIGSERIAL PRIMARY KEY,
  external_id VARCHAR(255),
  type VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  note TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  processed_at TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX ux_transactions_external_id ON transactions(external_id);

CREATE TABLE ledger_entries (
  id BIGSERIAL PRIMARY KEY,
  transaction_id BIGINT NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
  wallet_id BIGINT NOT NULL REFERENCES wallets(id),
  amount_bigint BIGINT NOT NULL,
  currency VARCHAR(8) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE audit_log (
  id BIGSERIAL PRIMARY KEY,
  aggregate_type VARCHAR(128),
  aggregate_id BIGINT,
  action VARCHAR(128) NOT NULL,
  payload JSONB,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE idempotency_keys (
  id BIGSERIAL PRIMARY KEY,
  idempotency_key VARCHAR(255) NOT NULL,
  endpoint VARCHAR(255) NOT NULL,
  status VARCHAR(32) NOT NULL,
  request_fingerprint VARCHAR(255),
  response_code INT,
  response_body TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  UNIQUE (idempotency_key, endpoint)
);

CREATE TABLE outbox (
  id BIGSERIAL PRIMARY KEY,
  aggregate_type VARCHAR(128),
  aggregate_id BIGINT,
  type VARCHAR(128) NOT NULL,
  payload TEXT,
  processed BOOLEAN DEFAULT false,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

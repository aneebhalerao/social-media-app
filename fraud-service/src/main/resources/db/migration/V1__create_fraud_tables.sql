CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── Fraud events log ──────────────────────────────────────────────
-- Permanent audit trail of every fraud determination.
-- Append-only — never update or delete rows here.
CREATE TABLE fraud_events (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    reason VARCHAR(100) NOT NULL,
    action_count INTEGER NOT NULL,
    detected_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_fraud_events PRIMARY KEY (id)
);

CREATE INDEX idx_fraud_events_user_id ON fraud_events (user_id);
CREATE INDEX idx_fraud_events_detected_at ON fraud_events (detected_at DESC);

-- ── Fraud status ──────────────────────────────────────────────────
-- Current fraud status per user.
-- One row per user, upserted when status changes.
CREATE TABLE fraud_status (
    user_id UUID NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'CLEAN',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_fraud_status  PRIMARY KEY (user_id),
    CONSTRAINT chk_fraud_status CHECK (status IN ('CLEAN', 'SUSPECT', 'FRAUD'))
);

CREATE INDEX idx_fraud_status_status ON fraud_status (status);

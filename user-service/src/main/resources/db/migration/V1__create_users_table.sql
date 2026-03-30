-- =================================================================
-- V1__create_users_table.sql
-- user-service schema — owns ONLY the users table
--
-- Key decisions:
--   extra_fields JSONB  → open-ended user-defined attributes
--                         stored as JSON, GIN-indexed for queries
--   fraud_status        → denormalised copy kept here so this
--                         service can answer "is user X fraud?"
--                         without calling fraud-service.
--                         Updated via Kafka consumer (eventual consistency).
-- =================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL,
    age SMALLINT NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    extra_fields JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT chk_users_age CHECK (age >= 16 AND age <= 120),
    CONSTRAINT chk_users_status
        CHECK (status IN ('ACTIVE', 'SUSPECT', 'FRAUD', 'DELETED'))
);

--UNIQUE constraint
ALTER TABLE users ADD CONSTRAINT uq_users_username UNIQUE (username);
--Indexes for fast lookup
CREATE INDEX idx_users_fraud_status ON users (status);
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_extra_fields ON users USING GIN (extra_fields);

-- =================================================================
-- Outbox table — Transactional Outbox Pattern
-- =================================================================
CREATE TABLE outbox_events (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(50)  NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ,
    CONSTRAINT pk_outbox PRIMARY KEY (id)
);

-- Poller reads this index: unpublished rows ordered by creation time
CREATE INDEX idx_outbox_unpublished
    ON outbox_events (created_at ASC)
    WHERE published = FALSE;
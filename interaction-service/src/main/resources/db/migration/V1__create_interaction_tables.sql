CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE profile_visits (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    visitor_id UUID NOT NULL,
    visited_id UUID NOT NULL,
    visit_count INTEGER NOT NULL DEFAULT 1,
    first_visited_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_visited_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_profile_visits PRIMARY KEY (id),
    CONSTRAINT uq_visit_pair UNIQUE (visitor_id, visited_id)
);

CREATE INDEX idx_visits_visited_id ON profile_visits (visited_id, last_visited_at DESC);
CREATE INDEX idx_visits_visitor_id ON profile_visits (visitor_id);

CREATE TABLE profile_likes (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    liker_id UUID NOT NULL,
    liked_id UUID NOT NULL,
    liked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_profile_likes PRIMARY KEY (id),
    CONSTRAINT uq_like_pair     UNIQUE (liker_id, liked_id)
);

CREATE INDEX idx_likes_liked_id  ON profile_likes (liked_id);
CREATE INDEX idx_likes_liker_id  ON profile_likes (liker_id);

CREATE TABLE blocked_users (
    user_id UUID NOT NULL,
    blocked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_blocked_users PRIMARY KEY (user_id)
);
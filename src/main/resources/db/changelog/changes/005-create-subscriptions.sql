--liquibase formatted sql

--changeset student:005
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,
    follower_id UUID NOT NULL,
    followed_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_not_self_subscriptions
        CHECK (follower_id <> followed_id),
    CONSTRAINT uq_follower_followed
        UNIQUE (follower_id, followed_id)
);

--rollback DROP TABLE subscriptions;
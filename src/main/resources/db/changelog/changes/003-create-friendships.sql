--liquibase formatted sql

--changeset student:003
CREATE TABLE friendships (
    id UUID PRIMARY KEY,
    first_user_id UUID NOT NULL,
    second_user_id UUID NOT NULL,
    offer_friendship_id UUID NOT NULL REFERENCES offer_friendships (id),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_canonical_order
        CHECK (first_user_id < second_user_id),
    CONSTRAINT uq_friendships_pair
        UNIQUE (first_user_id, second_user_id)
);

--rollback DROP TABLE friendships;
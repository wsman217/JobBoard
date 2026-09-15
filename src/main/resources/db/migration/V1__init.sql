CREATE TABLE users (
    id          UUID PRIMARY KEY,
    email       VARCHAR(255) NOT NULL,
    name        VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_users_email ON users (email);

CREATE TABLE user_oauth_accounts (
    id                BIGSERIAL    PRIMARY KEY,
    user_id           UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    provider          VARCHAR(32)  NOT NULL,
    provider_subject  VARCHAR(255) NOT NULL,
    provider_email    VARCHAR(255),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ux_oauth_provider_subject UNIQUE (provider, provider_subject)
);

CREATE INDEX ix_oauth_user_id ON user_oauth_accounts (user_id);

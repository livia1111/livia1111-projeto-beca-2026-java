CREATE TABLE users (
    id UUID PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_email_normalized CHECK (email = lower(btrim(email))),
    CONSTRAINT ck_users_email_not_blank CHECK (length(email) > 0),
    CONSTRAINT ck_users_nome_not_blank CHECK (length(btrim(nome)) > 0),
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'USER')),
    CONSTRAINT ck_users_timestamps CHECK (updated_at >= created_at)
);

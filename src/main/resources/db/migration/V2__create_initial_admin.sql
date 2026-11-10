-- Credencial inicial local: email admim@admin.com, senha admin (BCrypt).
INSERT INTO users (id, nome, email, senha_hash, role, created_at, updated_at)
VALUES (
    '5bd1913b-d683-4f67-84a4-4010363cc40f',
    'Administrador',
    'admim@admin.com',
    '$2a$12$apZoPQrdjusavxus2rbpuOIT5CsStdFy8/ffFQJZMe5SO8ViQHA5i',
    'ADMIN',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Corrige o e-mail do administrador inicial em bancos que já executaram a V2 antiga.
UPDATE users
SET email = 'admin@admin.com', updated_at = CURRENT_TIMESTAMP
WHERE id = '5bd1913b-d683-4f67-84a4-4010363cc40f'
  AND email = 'admim@admin.com';

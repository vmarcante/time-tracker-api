-- Reverte a criptografia do username na tabela TB0002_USER_AUTH

-- 1. Remover índice de hash
DROP INDEX IF EXISTS IX0002_USERNAME_HASH;

-- 2. Remover coluna de hash
ALTER TABLE TB0002_USER_AUTH DROP COLUMN IF EXISTS C0002_USERNAME_HASH;

-- 3. Reverter tipo da coluna criptografada
ALTER TABLE TB0002_USER_AUTH ALTER COLUMN C0002_USERNAME TYPE VARCHAR(100);

-- 4. Recriar constraint removida
ALTER TABLE TB0002_USER_AUTH ADD CONSTRAINT chk_username_length CHECK (LENGTH(TRIM(C0002_USERNAME)) >= 3);

-- 5. Recriar índice original de username
CREATE UNIQUE INDEX IX0002_USERNAME_LOWER ON TB0002_USER_AUTH (LOWER(C0002_USERNAME));

-- 6. Restaurar comentário original
COMMENT ON COLUMN TB0002_USER_AUTH.C0002_USERNAME IS 'Nome de usuário';

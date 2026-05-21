-- ATENÇÃO: Este script reverte as alterações em ordem inversa

-- Validar se o script foi executado antes de reverter
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM TB0000_SQL_HISTORY WHERE C0000_SCRIPT_NAME = '02-ddl-tb0002_user_auth.sql') THEN
        RAISE EXCEPTION 'Script 02-ddl-tb0002_user_auth.sql não foi executado. Não há nada para reverter.';
    END IF;
END $$;

-- 1. Revogar Permissões
REVOKE ALL ON TB0002_USER_AUTH FROM "TIME-TRACKER-API";
REVOKE ALL ON SEQUENCE seq_TB0002_USER_AUTH FROM "TIME-TRACKER-API";

-- 2. Remover Índices
DROP INDEX IF EXISTS IX0002_USERNAME_LOWER;
DROP INDEX IF EXISTS IX0002_SQ_ID;
DROP INDEX IF EXISTS IX0002_ACTIVE;
DROP INDEX IF EXISTS IX0002_CREATED_AT;

-- 3. Remover Constraints de FK
ALTER TABLE TB0002_USER_AUTH DROP CONSTRAINT IF EXISTS fk_user_id;

-- 4. Remover Tabelas
DROP TABLE IF EXISTS TB0002_USER_AUTH CASCADE;

-- 4. Remover Sequences
DROP SEQUENCE IF EXISTS seq_TB0002_USER_AUTH;

-- Remover o registro de execução do histórico
DELETE FROM TB0000_SQL_HISTORY WHERE C0000_SCRIPT_NAME = '02-ddl-tb0002_user_auth.sql';

-- ATENÇÃO: Este script reverte a tabela de histórico de scripts SQL
-- Deve ser executado por último, após reverter todos os outros scripts

-- 1. Revogar Permissões
REVOKE ALL ON TB0000_SQL_HISTORY FROM "TIME-TRACKER-API";
REVOKE ALL ON SEQUENCE tb0000_sql_history_c0000_history_id_seq FROM "TIME-TRACKER-API";

-- 2. Remover Índices
DROP INDEX IF EXISTS IX0000_EXECUTED_AT;
DROP INDEX IF EXISTS IX0000_SCRIPT_NAME;

-- 3. Remover Tabela
DROP TABLE IF EXISTS TB0000_SQL_HISTORY CASCADE;

-- Remove a FK única de time: o relacionamento N:N agora vive em TB0011_PROJECT_TEAM
ALTER TABLE TB0007_PROJECT DROP CONSTRAINT fk_project_team;
DROP INDEX IX0007_TEAM_ID;
ALTER TABLE TB0007_PROJECT DROP COLUMN C0007_TEAM_ID;

-- Índice único: não pode haver dois projetos ativos com o mesmo nome na mesma empresa
-- Case-insensitive e apenas entre projetos ativos
CREATE UNIQUE INDEX IX0007_COMPANY_NAME_UNIQUE
ON TB0007_PROJECT (C0007_COMPANY_ID, LOWER(C0007_NAME))
WHERE C0007_ACTIVE = TRUE;

-- Índice único: não pode haver dois times ativos com o mesmo nome na mesma empresa
-- Case-insensitive e apenas entre times ativos (times desativados não bloqueiam reuso do nome)
CREATE UNIQUE INDEX IX0006_COMPANY_NAME_UNIQUE
ON TB0006_TEAM (C0006_COMPANY_ID, LOWER(C0006_NAME))
WHERE C0006_ACTIVE = TRUE;

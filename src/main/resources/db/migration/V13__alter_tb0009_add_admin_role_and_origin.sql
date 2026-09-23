-- Adicionar ADMIN à hierarquia de roles e origem do vínculo em TB0009_USER_COMPANY

-- 1. Atualizar constraint de roles para incluir ADMIN
ALTER TABLE TB0009_USER_COMPANY
    DROP CONSTRAINT chk_user_company_role;

ALTER TABLE TB0009_USER_COMPANY
    ADD CONSTRAINT chk_user_company_role
    CHECK (C0009_ROLE IN ('OWNER', 'ADMIN', 'MANAGER', 'MEMBER'));

-- 2. Adicionar coluna de origem do vínculo
-- INVITE = empresa convidou o usuário (quem aprova é o usuário)
-- REQUEST = usuário solicitou entrada (quem aprova é a empresa)
ALTER TABLE TB0009_USER_COMPANY
    ADD COLUMN C0009_ORIGIN VARCHAR(10) NOT NULL DEFAULT 'REQUEST';

ALTER TABLE TB0009_USER_COMPANY
    ALTER COLUMN C0009_ORIGIN DROP DEFAULT;

ALTER TABLE TB0009_USER_COMPANY
    ADD CONSTRAINT chk_user_company_origin
    CHECK (C0009_ORIGIN IN ('INVITE', 'REQUEST'));

-- 3. Índice para listar pendentes por empresa
CREATE INDEX IX0009_COMPANY_PENDING
ON TB0009_USER_COMPANY (C0009_COMPANY_ID, C0009_ORIGIN, C0009_APPROVED)
WHERE C0009_ACTIVE = TRUE AND C0009_APPROVED = FALSE;

-- 4. Índice para listar convites pendentes do usuário
CREATE INDEX IX0009_USER_INVITATIONS
ON TB0009_USER_COMPANY (C0009_USER_ID, C0009_ORIGIN)
WHERE C0009_ACTIVE = TRUE AND C0009_APPROVED = FALSE;

-- 5. Comentários
COMMENT ON COLUMN TB0009_USER_COMPANY.C0009_ORIGIN IS 'Origem do vínculo: INVITE (empresa convidou, usuário aceita) ou REQUEST (usuário solicitou, empresa aprova)';

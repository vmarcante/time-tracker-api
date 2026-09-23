-- Adicionar data de aprovação do vínculo em TB0009_USER_COMPANY

ALTER TABLE TB0009_USER_COMPANY
    ADD COLUMN C0009_APPROVED_AT TIMESTAMP WITH TIME ZONE;

COMMENT ON COLUMN TB0009_USER_COMPANY.C0009_APPROVED_AT IS 'Data em que o vínculo foi aprovado (nulo enquanto pendente)';

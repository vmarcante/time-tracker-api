-- Adicionar coluna de documento (CNPJ) à empresa
ALTER TABLE TB0005_COMPANY
    ADD COLUMN C0005_DOCUMENT VARCHAR(18) NOT NULL DEFAULT '';

-- Remover o DEFAULT após adicionar (coluna passa a ser obrigatória sem default)
ALTER TABLE TB0005_COMPANY
    ALTER COLUMN C0005_DOCUMENT DROP DEFAULT;

-- Criar índice único para garantir unicidade por CNPJ
CREATE UNIQUE INDEX IX0005_DOCUMENT ON TB0005_COMPANY (C0005_DOCUMENT);

-- Comentário
COMMENT ON COLUMN TB0005_COMPANY.C0005_DOCUMENT IS 'CNPJ da empresa (formato: XX.XXX.XXX/XXXX-XX), único no sistema';

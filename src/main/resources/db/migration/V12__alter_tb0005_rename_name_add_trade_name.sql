-- Renomear C0005_NAME para C0005_LEGAL_NAME (Razão Social)
ALTER TABLE TB0005_COMPANY
    RENAME COLUMN C0005_NAME TO C0005_LEGAL_NAME;

-- Renomear índice correspondente
ALTER INDEX IX0005_NAME RENAME TO IX0005_LEGAL_NAME;

-- Adicionar coluna de Nome Fantasia (opcional)
ALTER TABLE TB0005_COMPANY
    ADD COLUMN C0005_TRADE_NAME VARCHAR(200);

-- Índice para busca por nome fantasia
CREATE INDEX IX0005_TRADE_NAME ON TB0005_COMPANY (C0005_TRADE_NAME);

-- Comentários
COMMENT ON COLUMN TB0005_COMPANY.C0005_LEGAL_NAME IS 'Razão social da empresa (nome legal registrado)';
COMMENT ON COLUMN TB0005_COMPANY.C0005_TRADE_NAME IS 'Nome fantasia da empresa (nome comercial, opcional)';

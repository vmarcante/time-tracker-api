# Padrão de Desenvolvimento de Database - Time Tracker API

Este documento define os padrões e convenções para criação e manutenção de scripts de banco de dados PostgreSQL.

---

## Índice

1. [Nomenclatura de Arquivos](#nomenclatura-de-arquivos)
2. [Nomenclatura de Objetos do Banco](#nomenclatura-de-objetos-do-banco)
3. [Estrutura de Scripts](#estrutura-de-scripts)
4. [Scripts de Revert](#scripts-de-revert)
5. [Boas Práticas](#boas-práticas)
6. [Exemplos](#exemplos)

---

## Nomenclatura de Arquivos

### **Padrão Geral**
```
[NN]-[tipo]-[nome_descritivo].sql
```

### **Componentes**
- **NN**: Número sequencial com 2 dígitos (01, 02, 03, ...)
- **tipo**: Tipo de operação (ddl, dml, migration, etc.)
- **nome_descritivo**: Descrição em snake_case do que o script faz

### **Tipos de Scripts**
| Tipo | Descrição | Exemplo |
|------|-----------|---------|
| `ddl` | Data Definition Language (CREATE, ALTER, DROP) | `01-ddl-TB0002_USER_AUTH.sql` |
| `dml` | Data Manipulation Language (INSERT, UPDATE, DELETE) | `02-dml-insert_initial_users.sql` |
| `migration` | Alterações em estruturas existentes | `03-migration-add_column_email.sql` |
| `seed` | Dados iniciais/teste | `04-seed-test_users.sql` |

### **Scripts de Revert Obrigatórios**
**Todo script DEVE ter um script de revert correspondente:**
```
01-ddl-TB0002_USER_AUTH.sql
01-ddl-TB0002_USER_AUTH_revert.sql  ← Obrigatório
```

---

## Nomenclatura de Objetos do Banco

### **1. Tabelas**
```
TB[NNNN]_[NOME_MAIUSCULO]
```

**Exemplo:**
```sql
TB0002_USER_AUTH
TB0002_USER_PROFILE
TB0003_PRODUCT
```

**Regras:**
- Prefixo `TB` seguido de 4 dígitos
- Nome em MAIÚSCULAS separado por underscore
- Nome descritivo e singular quando possível

---

### **2. Colunas**
```
C[NNNN]_[NOME_MAIUSCULO]
```

**Exemplo:**
```sql
C0001_ID
C0001_USERNAME
C0001_CREATED_AT
```

**Regras:**
- Prefixo `C` seguido de 4 dígitos (número da tabela)
- Nome em MAIÚSCULAS separado por underscore
- Usar sufixos padrão:
  - `_ID` para identificadores
  - `_AT` para timestamps
  - `_FLAG` ou `_REQUESTED` para booleanos

**Colunas Padrão (Auditoria):**
```sql
C[NNNN]_CREATED_AT TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
C[NNNN]_UPDATED_AT TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
```

---

### **3. Sequences**
```
seq_tb[nnnn]_[nome_minusculo]
```

**Exemplo:**
```sql
CREATE SEQUENCE seq_TB0002_USER_AUTH START 1;
```

**Regras:**
- Prefixo `seq_tb` seguido do número da tabela
- Nome em minúsculas separado por underscore
- Sempre iniciar em 1

---

### **4. Índices**
```
IX[NNNN]_[NOME_MAIUSCULO]
```

**Exemplo:**
```sql
CREATE UNIQUE INDEX IX0001_USERNAME_LOWER ON TB0002_USER_AUTH (LOWER(C0001_USERNAME));
CREATE INDEX IX0001_ACTIVE ON TB0002_USER_AUTH (C0001_ACTIVE);
```

**Regras:**
- Prefixo `IX` seguido de 4 dígitos (número da tabela)
- Nome descritivo do campo ou condição em MAIÚSCULAS
- Usar `UNIQUE` quando aplicável

**Índices Recomendados:**
- Primary Key (automático)
- Foreign Keys
- Colunas de busca frequente
- Colunas de filtro (WHERE)
- Colunas de ordenação (ORDER BY)

---

### **5. Constraints**
```
chk_[nome_descritivo]
fk_[tabela_origem]_[tabela_destino]
```

**Exemplo:**
```sql
CONSTRAINT chk_username_length CHECK (LENGTH(TRIM(C0001_USERNAME)) >= 3)
CONSTRAINT chk_failed_attempts CHECK (C0001_FAILED_ATTEMPTS >= 0)
CONSTRAINT fk_user_profile_user_auth FOREIGN KEY (C0002_USER_ID) REFERENCES TB0002_USER_AUTH(C0002_USER_ID)
```

**Regras:**
- `chk_` para CHECK constraints
- `fk_` para FOREIGN KEY constraints
- Nome descritivo em minúsculas

---

## Estrutura de Scripts

### **Template Padrão**

```sql
-- 1. Criar Sequences (se necessário)
CREATE SEQUENCE seq_tb[nnnn]_[nome] START 1;

-- 2. Criar Tabelas
CREATE TABLE TB[NNNN]_[NOME] (
    C[NNNN]_[CAMPO]_ID UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    C[NNNN]_[CAMPO] VARCHAR(100) NOT NULL,
    C[NNNN]_CREATED_AT TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    C[NNNN]_UPDATED_AT TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    C[NNNN]_SQ_ID INT NOT NULL DEFAULT nextval('seq_tb[nnnn]_[nome]'),
    CONSTRAINT chk_[nome] CHECK ([condição])
);

-- 3. Criar Índices
CREATE UNIQUE INDEX IX[NNNN]_[CAMPO] ON TB[NNNN]_[NOME] (C[NNNN]_[CAMPO]);
CREATE INDEX IX[NNNN]_[CAMPO] ON TB[NNNN]_[NOME] (C[NNNN]_[CAMPO]);

-- 4. Adicionar Comentários
COMMENT ON TABLE TB[NNNN]_[NOME] IS '[Descrição da tabela]';
COMMENT ON COLUMN TB[NNNN]_[NOME].C[NNNN]_[CAMPO] IS '[Descrição do campo]';

-- 5. Conceder Permissões
GRANT SELECT, INSERT, UPDATE, DELETE ON TB[NNNN]_[NOME] TO "TIME-TRACKER-API";
GRANT USAGE, SELECT ON SEQUENCE seq_tb[nnnn]_[nome] TO "TIME-TRACKER-API";
```

---

## Scripts de Revert

### **Obrigatoriedade**
**TODO script de alteração DEVE ter um script de revert correspondente.**

### **Nomenclatura**
```
[NN]-[tipo]-[nome]_revert.sql
```

### **Estrutura do Revert**

```sql
-- ATENÇÃO: Este script reverte as alterações em ordem inversa

-- 1. Revogar Permissões
REVOKE ALL ON TB[NNNN]_[NOME] FROM "TIME-TRACKER-API";
REVOKE ALL ON SEQUENCE seq_tb[nnnn]_[nome] FROM "TIME-TRACKER-API";

-- 2. Remover Índices
DROP INDEX IF EXISTS IX[NNNN]_[CAMPO];

-- 3. Remover Tabelas
DROP TABLE IF EXISTS TB[NNNN]_[NOME] CASCADE;

-- 4. Remover Sequences
DROP SEQUENCE IF EXISTS seq_tb[nnnn]_[nome];
```

### **Ordem de Execução no Revert**
1. Revogar permissões
2. Remover índices
3. Remover foreign keys
4. Remover tabelas (CASCADE se necessário)
5. Remover sequences

---

## Boas Práticas

### **1. Tipos de Dados**
| Tipo Java/Domain | Tipo PostgreSQL | Observação |
|------------------|-----------------|------------|
| `UUID` | `UUID` | Usar `gen_random_uuid()` como default |
| `String` | `VARCHAR(n)` | Definir tamanho apropriado |
| `LocalDateTime` | `TIMESTAMP WITH TIME ZONE` | Sempre com timezone |
| `Boolean` | `BOOLEAN` | Definir default (TRUE/FALSE) |
| `Integer` | `INT` | Usar CHECK constraints quando aplicável |
| `Long` | `BIGINT` | Para valores grandes |

### **2. Campos Obrigatórios**
```sql
-- Toda tabela DEVE ter:
C[NNNN]_[ID] UUID PRIMARY KEY DEFAULT gen_random_uuid()
C[NNNN]_CREATED_AT TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
C[NNNN]_UPDATED_AT TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
C[NNNN]_SQ_ID INT NOT NULL DEFAULT nextval('seq_tb[nnnn]_[nome]')
```

### **3. Constraints**
- Sempre adicionar CHECK constraints para validações básicas
- Usar NOT NULL quando o campo é obrigatório
- Definir DEFAULT values quando apropriado
- Nomear todas as constraints explicitamente

### **4. Índices**
- Criar índice UNIQUE para campos de busca únicos
- Criar índice para foreign keys
- Criar índice para campos de filtro frequente
- Usar índices parciais (WHERE) quando possível

### **5. Comentários**
- **SEMPRE** adicionar comentários em tabelas e colunas
- Comentários devem ser descritivos e claros
- Explicar o propósito e uso do campo

### **6. Permissões**
```sql
-- Usuário da aplicação
GRANT SELECT, INSERT, UPDATE, DELETE ON [TABELA] TO "TIME-TRACKER-API";
GRANT USAGE, SELECT ON SEQUENCE [SEQUENCE] TO "TIME-TRACKER-API";
```

### **7. Segurança**
- Nunca usar `DROP TABLE` sem `IF EXISTS`
- Sempre usar `CASCADE` com cuidado
- Testar scripts em ambiente de desenvolvimento primeiro
- Manter backup antes de executar scripts de produção

---

## Exemplos

### **Exemplo 1: Script DDL Completo**
```sql
CREATE SEQUENCE seq_TB0002_USER_AUTH START 1;

CREATE TABLE TB0002_USER_AUTH (
    C0001_ID UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    C0001_USERNAME VARCHAR(100) NOT NULL,
    C0001_PASSWORD_HASH VARCHAR(512) NOT NULL,
    C0001_PASSWORD_SALT VARCHAR(512) NOT NULL,
    C0001_CREATED_AT TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    C0001_UPDATED_AT TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    C0001_ACTIVE BOOLEAN DEFAULT TRUE NOT NULL,
    C0001_SQ_ID INT NOT NULL DEFAULT nextval('seq_TB0002_USER_AUTH'),
    CONSTRAINT chk_username_length CHECK (LENGTH(TRIM(C0001_USERNAME)) >= 3)
);

CREATE UNIQUE INDEX IX0001_USERNAME_LOWER ON TB0002_USER_AUTH (LOWER(C0001_USERNAME));
CREATE INDEX IX0001_ACTIVE ON TB0002_USER_AUTH (C0001_ACTIVE) WHERE C0001_ACTIVE = TRUE;

COMMENT ON TABLE TB0002_USER_AUTH IS 'Tabela de autenticação de usuários';
COMMENT ON COLUMN TB0002_USER_AUTH.C0001_ID IS 'Identificador único do usuário';

GRANT SELECT, INSERT, UPDATE, DELETE ON TB0002_USER_AUTH TO "TIME-TRACKER-API";
GRANT USAGE, SELECT ON SEQUENCE seq_TB0002_USER_AUTH TO "TIME-TRACKER-API";
```

### **Exemplo 2: Script de Revert**
```sql
REVOKE ALL ON TB0002_USER_AUTH FROM "TIME-TRACKER-API";
REVOKE ALL ON SEQUENCE seq_TB0002_USER_AUTH FROM "TIME-TRACKER-API";

DROP INDEX IF EXISTS IX0001_USERNAME_LOWER;
DROP INDEX IF EXISTS IX0001_ACTIVE;

DROP TABLE IF EXISTS TB0002_USER_AUTH CASCADE;

DROP SEQUENCE IF EXISTS seq_TB0002_USER_AUTH;
```

### **Exemplo 3: Script de Migration**
```sql
ALTER TABLE TB0002_USER_AUTH 
ADD COLUMN C0001_EMAIL VARCHAR(255);

CREATE INDEX IX0001_EMAIL ON TB0002_USER_AUTH (C0001_EMAIL);

COMMENT ON COLUMN TB0002_USER_AUTH.C0001_EMAIL IS 'Endereço de email do usuário';
```

### **Exemplo 4: Script de Revert de Migration**
```sql
DROP INDEX IF EXISTS IX0001_EMAIL;

ALTER TABLE TB0002_USER_AUTH 
DROP COLUMN IF EXISTS C0001_EMAIL;
```

---

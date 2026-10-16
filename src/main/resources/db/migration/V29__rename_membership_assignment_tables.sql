-- Padronização: renomear tabelas/objetos para refletir o nome das entities
-- TB0009_USER_COMPANY   -> TB0009_COMPANY_MEMBERSHIP  (CompanyMembershipJpaEntity)
-- TB0010_USER_TEAM      -> TB0010_TEAM_MEMBERSHIP     (TeamMembershipJpaEntity)
-- TB0012_USER_PROJECT   -> TB0012_PROJECT_ASSIGNMENT  (ProjectAssignmentJpaEntity)
-- Colunas C####_ e índices numéricos IX####_ permanecem (amarrados ao número da tabela)

-- ============================================================================
-- TB0009: user_company -> company_membership
-- ============================================================================
ALTER TABLE TB0009_USER_COMPANY RENAME TO TB0009_COMPANY_MEMBERSHIP;
ALTER SEQUENCE seq_tb0009_user_company RENAME TO seq_tb0009_company_membership;

ALTER TABLE TB0009_COMPANY_MEMBERSHIP RENAME CONSTRAINT chk_user_company_role TO chk_company_membership_role;
ALTER TABLE TB0009_COMPANY_MEMBERSHIP RENAME CONSTRAINT chk_user_company_origin TO chk_company_membership_origin;
ALTER TABLE TB0009_COMPANY_MEMBERSHIP RENAME CONSTRAINT fk_user_company_user TO fk_company_membership_user;
ALTER TABLE TB0009_COMPANY_MEMBERSHIP RENAME CONSTRAINT fk_user_company_company TO fk_company_membership_company;
ALTER TABLE TB0009_COMPANY_MEMBERSHIP RENAME CONSTRAINT fk_user_company_created_by TO fk_company_membership_created_by;
ALTER TABLE TB0009_COMPANY_MEMBERSHIP RENAME CONSTRAINT fk_user_company_updated_by TO fk_company_membership_updated_by;
ALTER TABLE TB0009_COMPANY_MEMBERSHIP RENAME CONSTRAINT fk_user_company_approved_by TO fk_company_membership_approved_by;

ALTER INDEX IX0009_USER_COMPANY_UNIQUE RENAME TO IX0009_COMPANY_MEMBERSHIP_UNIQUE;
ALTER INDEX IX0009_USER_COMPANY_APPROVED RENAME TO IX0009_COMPANY_MEMBERSHIP_APPROVED;

COMMENT ON TABLE TB0009_COMPANY_MEMBERSHIP IS 'Vínculo (membership) entre usuários e empresas';

-- ============================================================================
-- TB0010: user_team -> team_membership
-- ============================================================================
ALTER TABLE TB0010_USER_TEAM RENAME TO TB0010_TEAM_MEMBERSHIP;
ALTER SEQUENCE seq_tb0010_user_team RENAME TO seq_tb0010_team_membership;

ALTER TABLE TB0010_TEAM_MEMBERSHIP RENAME CONSTRAINT chk_user_team_role TO chk_team_membership_role;
ALTER TABLE TB0010_TEAM_MEMBERSHIP RENAME CONSTRAINT fk_user_team_user TO fk_team_membership_user;
ALTER TABLE TB0010_TEAM_MEMBERSHIP RENAME CONSTRAINT fk_user_team_team TO fk_team_membership_team;
ALTER TABLE TB0010_TEAM_MEMBERSHIP RENAME CONSTRAINT fk_user_team_created_by TO fk_team_membership_created_by;
ALTER TABLE TB0010_TEAM_MEMBERSHIP RENAME CONSTRAINT fk_user_team_updated_by TO fk_team_membership_updated_by;

ALTER INDEX IX0010_USER_TEAM_UNIQUE RENAME TO IX0010_TEAM_MEMBERSHIP_UNIQUE;
ALTER INDEX IX0010_USER_TEAM_APPROVED RENAME TO IX0010_TEAM_MEMBERSHIP_APPROVED;

COMMENT ON TABLE TB0010_TEAM_MEMBERSHIP IS 'Vínculo (membership) entre usuários e times';

-- ============================================================================
-- TB0012: user_project -> project_assignment
-- ============================================================================
ALTER TABLE TB0012_USER_PROJECT RENAME TO TB0012_PROJECT_ASSIGNMENT;
ALTER SEQUENCE seq_tb0012_user_project RENAME TO seq_tb0012_project_assignment;

ALTER TABLE TB0012_PROJECT_ASSIGNMENT RENAME CONSTRAINT fk_user_project_user TO fk_project_assignment_user;
ALTER TABLE TB0012_PROJECT_ASSIGNMENT RENAME CONSTRAINT fk_user_project_project TO fk_project_assignment_project;
ALTER TABLE TB0012_PROJECT_ASSIGNMENT RENAME CONSTRAINT fk_user_project_team TO fk_project_assignment_team;
ALTER TABLE TB0012_PROJECT_ASSIGNMENT RENAME CONSTRAINT fk_user_project_created_by TO fk_project_assignment_created_by;
ALTER TABLE TB0012_PROJECT_ASSIGNMENT RENAME CONSTRAINT fk_user_project_updated_by TO fk_project_assignment_updated_by;

ALTER INDEX IX0012_USER_PROJECT_TEAM_UNIQUE RENAME TO IX0012_PROJECT_ASSIGNMENT_UNIQUE;

COMMENT ON TABLE TB0012_PROJECT_ASSIGNMENT IS 'Atribuição de projetos a membros de times';

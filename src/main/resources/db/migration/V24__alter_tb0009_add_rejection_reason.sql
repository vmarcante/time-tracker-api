-- Justificativa opcional informada pelo aprovador ao reprovar uma solicitação de vínculo
ALTER TABLE TB0009_USER_COMPANY
    ADD COLUMN C0009_REJECTION_REASON VARCHAR(500);

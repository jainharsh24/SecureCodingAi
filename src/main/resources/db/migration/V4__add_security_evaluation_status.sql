ALTER TABLE security_evaluations
    ADD COLUMN evaluator_status VARCHAR(32),
    ADD COLUMN evaluator_error TEXT;

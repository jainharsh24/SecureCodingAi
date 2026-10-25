ALTER TABLE security_evaluations
    ADD COLUMN assessment TEXT,
    ADD COLUMN remediation TEXT,
    ADD COLUMN feedback_summary TEXT;

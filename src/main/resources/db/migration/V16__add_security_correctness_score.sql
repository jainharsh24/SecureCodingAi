ALTER TABLE submissions
    ADD COLUMN security_score INTEGER,
    ADD COLUMN security_correctness_status VARCHAR(64);

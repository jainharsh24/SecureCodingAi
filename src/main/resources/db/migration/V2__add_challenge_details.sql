ALTER TABLE challenges
    ADD COLUMN vulnerability_subtype VARCHAR(100),
    ADD COLUMN cwe VARCHAR(32),
    ADD COLUMN starter_code TEXT;

ALTER TABLE test_cases
    ADD COLUMN position INTEGER NOT NULL DEFAULT 0;

CREATE INDEX idx_test_cases_challenge_position ON test_cases(challenge_id, position);

ALTER TABLE users ADD COLUMN name VARCHAR(120);
UPDATE users SET name = split_part(email, '@', 1) WHERE name IS NULL;
ALTER TABLE users ALTER COLUMN name SET NOT NULL;

ALTER TABLE challenges ADD COLUMN difficulty VARCHAR(16) NOT NULL DEFAULT 'MEDIUM';
ALTER TABLE challenges ADD CONSTRAINT chk_challenge_difficulty CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD'));

ALTER TABLE submissions ADD COLUMN attempt_number INTEGER NOT NULL DEFAULT 1;
ALTER TABLE submissions ADD COLUMN learning_score INTEGER NOT NULL DEFAULT 0;
ALTER TABLE submissions ADD COLUMN raw_assessment TEXT;
WITH numbered AS (
    SELECT id, row_number() OVER (PARTITION BY user_id, challenge_id ORDER BY submitted_at, id) AS attempt
    FROM submissions
)
UPDATE submissions SET attempt_number = numbered.attempt FROM numbered WHERE submissions.id = numbered.id;
ALTER TABLE submissions ADD CONSTRAINT chk_submission_learning_score CHECK (learning_score BETWEEN 0 AND 100);
ALTER TABLE submissions ADD CONSTRAINT uq_submission_user_challenge_attempt UNIQUE (user_id, challenge_id, attempt_number);
CREATE INDEX idx_submissions_user_challenge ON submissions(user_id, challenge_id, attempt_number);

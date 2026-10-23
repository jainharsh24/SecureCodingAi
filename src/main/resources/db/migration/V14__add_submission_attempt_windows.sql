CREATE TABLE submission_attempt_windows (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    challenge_id BIGINT NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    started_at TIMESTAMPTZ NOT NULL,
    attempts_used INTEGER NOT NULL DEFAULT 0 CHECK (attempts_used BETWEEN 0 AND 5),
    PRIMARY KEY (user_id, challenge_id)
);
CREATE INDEX idx_submission_attempt_windows_started_at ON submission_attempt_windows (started_at);

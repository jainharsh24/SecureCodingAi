-- A submission assessment is a short-lived capability, not a challenge-wide retry.
ALTER TABLE submissions ADD COLUMN understanding_assessment_session_token VARCHAR(64);
CREATE UNIQUE INDEX idx_submissions_understanding_session
    ON submissions(understanding_assessment_session_token)
    WHERE understanding_assessment_session_token IS NOT NULL;

-- An explanation is evidence for one submitted solution, not every solution to a challenge.
ALTER TABLE understanding_assessments ADD COLUMN submission_id BIGINT REFERENCES submissions(id);
CREATE INDEX idx_understanding_assessments_submission ON understanding_assessments(submission_id, id);

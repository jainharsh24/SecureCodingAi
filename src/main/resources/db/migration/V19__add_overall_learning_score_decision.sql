ALTER TABLE submissions
    ADD COLUMN understanding_score INTEGER,
    ADD COLUMN understanding_assessment_status VARCHAR(32),
    ADD COLUMN verified_final_score INTEGER,
    ADD COLUMN learning_score_state VARCHAR(64),
    ADD COLUMN challenge_completed BOOLEAN,
    ADD COLUMN challenge_mastered BOOLEAN;

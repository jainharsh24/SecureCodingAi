package com.securecode.ai.evaluation.groq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.Difficulty;
import com.securecode.ai.evaluation.groq.config.GroqProperties;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class GroqUnderstandingEvaluatorTests {
    private final GroqProperties properties = new GroqProperties(true, "https://example.test", "model", "key", 1_000, 200, 4096, 0, true);

    @Test
    void acceptsOnlyStructuredZeroOrFiveRubricPoints() {
        GroqUnderstandingEvaluator evaluator = evaluator((system, prompt) -> response(5, 0, 5));

        var result = evaluator.evaluate(challenge(), "Explain SQL injection", "answer");

        assertThat(result.rootCausePoints()).isEqualTo(5);
        assertThat(result.mechanismPoints()).isZero();
        assertThat(result.preventionPoints()).isEqualTo(5);
    }

    @Test
    void rejectsInvalidStructuredScores() {
        GroqUnderstandingEvaluator evaluator = evaluator((system, prompt) -> response(4, 5, 5));

        assertThatThrownBy(() -> evaluator.evaluate(challenge(), "question", "answer"))
                .isInstanceOf(GroqProviderException.class)
                .hasMessageContaining("invalid structured understanding");
    }

    private GroqUnderstandingEvaluator evaluator(GroqClient client) {
        return new GroqUnderstandingEvaluator(properties, client, JsonMapper.builder().build());
    }
    private Challenge challenge() { return new Challenge(1, "SQL", "Use parameters", "java", "Injection", "SQL Injection", "CWE-89", "", Difficulty.EASY); }
    private String response(int root, int mechanism, int prevention) {
        return "{\"root_cause_points\":" + root + ",\"mechanism_points\":" + mechanism + ",\"prevention_points\":" + prevention
                + ",\"root_cause_feedback\":\"root\",\"mechanism_feedback\":\"mechanism\",\"prevention_feedback\":\"prevention\"}";
    }
}

package com.securecode.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.Difficulty;
import com.securecode.ai.entity.Role;
import com.securecode.ai.entity.SecurityEvaluation;
import com.securecode.ai.entity.Submission;
import com.securecode.ai.entity.User;
import com.securecode.ai.evaluation.SecurityCorrectnessStatus;
import com.securecode.ai.evaluation.SecurityFinding;
import com.securecode.ai.evaluation.common.SecurityFindingEvidenceCodec;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class SecurityCorrectnessScoringServiceTests {
    private final SecurityFindingEvidenceCodec codec = new SecurityFindingEvidenceCodec(JsonMapper.builder().build());
    private final SecurityCorrectnessScoringService service = new SecurityCorrectnessScoringService(codec);

    @Test
    void awardsAllSecurityPointsOnlyForCompletedAndVerifiedSafeEvidence() {
        SecurityCorrectnessResult result = service.score(sqlChallenge(), List.of(
                evaluation("SEMGREP", false, List.of(), null),
                evaluation("SPOTBUGS", false, List.of(), null),
                evaluation("GROQ", false, List.of(), "SAFE")));

        assertThat(result.score()).isEqualTo(45);
        assertThat(result.status()).isEqualTo(SecurityCorrectnessStatus.VERIFIED_SAFE);
        assertThat(result.blocksMastery()).isFalse();
    }

    @Test
    void confirmedTargetedVulnerabilityGetsNoPointsAndBlocksMastery() {
        SecurityFinding sql = finding("SQL Injection", "CWE-89");
        SecurityCorrectnessResult result = service.score(sqlChallenge(), List.of(
                evaluation("SEMGREP", true, List.of(sql), null),
                evaluation("SPOTBUGS", true, List.of(sql), null),
                evaluation("GROQ", true, List.of(sql), "VULNERABLE")));

        assertThat(result.score()).isZero();
        assertThat(result.status()).isEqualTo(SecurityCorrectnessStatus.TARGETED_VULNERABILITY);
        assertThat(result.blocksMastery()).isTrue();
        assertThat(result.targetedFindingKeys()).hasSize(1);
    }

    @Test
    void mixedValidVerdictsReceiveProportionalCreditButAConfirmedVulnerabilityBlocksMastery() {
        SecurityCorrectnessResult result = service.score(sqlChallenge(), List.of(
                evaluation("SEMGREP", true, List.of(finding("SQL Injection", "CWE-89")), null),
                evaluation("SPOTBUGS", false, List.of(), null),
                evaluation("GROQ", false, List.of(), "SAFE")));

        assertThat(result.score()).isEqualTo(30);
        assertThat(result.status()).isEqualTo(SecurityCorrectnessStatus.TARGETED_VULNERABILITY);
        assertThat(result.blocksMastery()).isTrue();
    }

    @Test
    void unavailableEvaluatorsDoNotBlockVerifiedSafeAvailableStaticEvidence() {
        SecurityCorrectnessResult result = service.score(sqlChallenge(), List.of(
                evaluation("SEMGREP", false, List.of(), null),
                evaluation("SPOTBUGS", false, List.of(), null),
                unavailable("GROQ")));

        assertThat(result.score()).isEqualTo(45);
        assertThat(result.status()).isEqualTo(SecurityCorrectnessStatus.VERIFIED_SAFE);
        assertThat(result.blocksMastery()).isFalse();
    }

    @Test
    void aSingleValidSafeVerdictReceivesTheEntireSecurityPointPool() {
        SecurityCorrectnessResult result = service.score(sqlChallenge(), List.of(
                unavailable("SEMGREP"), unavailable("SPOTBUGS"), evaluation("GROQ", false, List.of(), "SAFE")));

        assertThat(result.score()).isEqualTo(45);
        assertThat(result.status()).isEqualTo(SecurityCorrectnessStatus.VERIFIED_SAFE);
    }

    @Test
    void roundsAOneOfTwoSafeSplitConsistently() {
        SecurityCorrectnessResult result = service.score(sqlChallenge(), List.of(
                evaluation("SEMGREP", false, List.of(), null),
                evaluation("SPOTBUGS", true, List.of(finding("SQL Injection", "CWE-89")), null)));

        assertThat(result.score()).isEqualTo(23);
        assertThat(result.status()).isEqualTo(SecurityCorrectnessStatus.TARGETED_VULNERABILITY);
    }

    @Test
    void awardsZeroForValidFailuresAndLeavesNoValidResultUnverifiedWithoutAZeroScore() {
        SecurityCorrectnessResult failed = service.score(sqlChallenge(), List.of(
                evaluation("SEMGREP", true, List.of(finding("SQL Injection", "CWE-89")), null)));
        SecurityCorrectnessResult unavailable = service.score(sqlChallenge(), List.of(
                unavailable("SEMGREP"), unavailable("SPOTBUGS"), malformedGroq()));

        assertThat(failed.score()).isZero();
        assertThat(failed.status()).isEqualTo(SecurityCorrectnessStatus.TARGETED_VULNERABILITY);
        assertThat(unavailable.score()).isNull();
        assertThat(unavailable.status()).isEqualTo(SecurityCorrectnessStatus.UNVERIFIED);
    }

    private Challenge sqlChallenge() {
        return new Challenge(1, "SQL", "Use parameters", "java", "Injection", "SQL Injection", "CWE-89", "", Difficulty.EASY);
    }

    private SecurityEvaluation evaluation(String evaluator, boolean detected, List<SecurityFinding> findings, String assessment) {
        return new SecurityEvaluation(submission(), evaluator, detected, detected ? "SQL Injection" : null,
                detected ? "CWE-89" : null, detected ? "HIGH" : "INFO", null, codec.encode(findings), "COMPLETED", null,
                assessment, null, null);
    }

    private SecurityEvaluation unavailable(String evaluator) {
        return new SecurityEvaluation(submission(), evaluator, false, null, null, "EVALUATOR_ERROR", null,
                codec.encode(List.of()), "TIMEOUT", "timed out");
    }

    private SecurityEvaluation malformedGroq() {
        return new SecurityEvaluation(submission(), "GROQ", false, null, null, "INFO", null,
                codec.encode(List.of()), "COMPLETED", "invalid response", null, null, null);
    }

    private SecurityFinding finding(String type, String cwe) {
        return new SecurityFinding("rule", "message", "HIGH", type, cwe, "Main.java", 3, null, null, null, null);
    }

    private Submission submission() {
        return new Submission(new User("student@example.com", "unused", Role.STUDENT), sqlChallenge(), "class Main {}", 1);
    }
}

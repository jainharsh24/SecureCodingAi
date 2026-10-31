package com.securecode.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.securecode.ai.entity.FunctionalEvaluationStatus;
import com.securecode.ai.entity.UnderstandingAssessmentStatus;
import com.securecode.ai.evaluation.SecurityCorrectnessStatus;
import org.junit.jupiter.api.Test;

class OverallLearningScoreServiceTests {
    private final OverallLearningScoreService service = new OverallLearningScoreService();

    @Test
    void combinesAllFourVerifiedComponentsAndDeclaresMastery() {
        var result = score(FunctionalEvaluationStatus.PASS, 25, 45, SecurityCorrectnessStatus.VERIFIED_SAFE,
                15, UnderstandingAssessmentStatus.ASSESSED, 15);
        assertThat(result.displayedScore()).isEqualTo(100);
        assertThat(result.verifiedFinalScore()).isEqualTo(100);
        assertThat(result.provisional()).isFalse();
        assertThat(result.challengeCompleted()).isTrue();
        assertThat(result.challengeMastered()).isTrue();
    }

    @Test
    void confirmedTargetedVulnerabilityCapsScoreAndBlocksCompletionAndMastery() {
        var result = score(FunctionalEvaluationStatus.PASS, 25, 45, SecurityCorrectnessStatus.TARGETED_VULNERABILITY,
                15, UnderstandingAssessmentStatus.ASSESSED, 15);
        assertThat(result.displayedScore()).isEqualTo(49);
        assertThat(result.scoreState()).isEqualTo("TARGETED_VULNERABILITY");
        assertThat(result.challengeCompleted()).isFalse();
        assertThat(result.challengeMastered()).isFalse();
    }

    @Test
    void unavailableSecurityEvidenceProducesOnlyAProvisionalScore() {
        var result = score(FunctionalEvaluationStatus.PASS, 25, null, SecurityCorrectnessStatus.UNVERIFIED,
                15, UnderstandingAssessmentStatus.ASSESSED, 15);
        assertThat(result.displayedScore()).isEqualTo(55);
        assertThat(result.verifiedFinalScore()).isNull();
        assertThat(result.provisional()).isTrue();
        assertThat(result.scoreState()).isEqualTo("UNVERIFIED");
        assertThat(result.challengeCompleted()).isFalse();
    }

    @Test
    void conflictingEvidenceBlocksVerifiedCompletionAndMastery() {
        var result = score(FunctionalEvaluationStatus.PASS, 25, 0, SecurityCorrectnessStatus.CONFLICTING_EVIDENCE,
                15, UnderstandingAssessmentStatus.ASSESSED, 15);
        assertThat(result.scoreState()).isEqualTo("CONFLICTING_EVIDENCE");
        assertThat(result.provisional()).isTrue();
        assertThat(result.challengeCompleted()).isFalse();
        assertThat(result.challengeMastered()).isFalse();
    }

    @Test
    void functionalFailureBlocksCompletionEvenWhenSecurityIsSafe() {
        var result = score(FunctionalEvaluationStatus.FAIL, 20, 45, SecurityCorrectnessStatus.VERIFIED_SAFE,
                15, UnderstandingAssessmentStatus.ASSESSED, 15);
        assertThat(result.verifiedFinalScore()).isNull();
        assertThat(result.provisional()).isTrue();
        assertThat(result.challengeCompleted()).isFalse();
        assertThat(result.challengeMastered()).isFalse();
    }

    @Test
    void partialUnderstandingCanCompleteButCannotMaster() {
        var result = score(FunctionalEvaluationStatus.PASS, 25, 45, SecurityCorrectnessStatus.VERIFIED_SAFE,
                10, UnderstandingAssessmentStatus.ASSESSED, 15);
        assertThat(result.challengeCompleted()).isTrue();
        assertThat(result.challengeMastered()).isFalse();
    }

    @Test
    void recordedAssistancePreventsMasteryEvenWhenTheRawScoreMeetsTheThreshold() {
        var result = score(FunctionalEvaluationStatus.PASS, 25, 45, SecurityCorrectnessStatus.VERIFIED_SAFE,
                15, UnderstandingAssessmentStatus.ASSESSED, 5);

        assertThat(result.displayedScore()).isEqualTo(90);
        assertThat(result.challengeCompleted()).isTrue();
        assertThat(result.challengeMastered()).isFalse();
        assertThat(result.reasons()).contains("Recorded assistance prevents independent mastery.");
    }

    @Test
    void availableVerifiedSecurityEvidenceSolvesWithoutUnderstandingOrIndependence() {
        var result = score(FunctionalEvaluationStatus.PASS, 25, 45, SecurityCorrectnessStatus.VERIFIED_SAFE,
                null, null, null);

        assertThat(result.displayedScore()).isEqualTo(70);
        assertThat(result.challengeCompleted()).isTrue();
        assertThat(result.challengeMastered()).isFalse();
    }

    @Test
    void verifiedStatusesCannotSolveBelowTheFunctionalAndSecurityBaseline() {
        var result = score(FunctionalEvaluationStatus.PASS, 24, 45, SecurityCorrectnessStatus.VERIFIED_SAFE,
                15, UnderstandingAssessmentStatus.ASSESSED, 15);

        assertThat(result.displayedScore()).isEqualTo(99);
        assertThat(result.challengeCompleted()).isFalse();
        assertThat(result.challengeMastered()).isFalse();
    }

    @Test
    void masteryRequiresNinetyPointsAndScoresAboveTenForUnderstandingAndIndependence() {
        var result = score(FunctionalEvaluationStatus.PASS, 25, 45, SecurityCorrectnessStatus.VERIFIED_SAFE,
                11, UnderstandingAssessmentStatus.ASSESSED, 11);

        assertThat(result.displayedScore()).isEqualTo(92);
        assertThat(result.challengeMastered()).isTrue();
    }

    @Test
    void masteryRejectsTenPointUnderstandingOrIndependence() {
        var result = score(FunctionalEvaluationStatus.PASS, 25, 45, SecurityCorrectnessStatus.VERIFIED_SAFE,
                10, UnderstandingAssessmentStatus.ASSESSED, 15);

        assertThat(result.challengeMastered()).isFalse();
    }

    private OverallLearningScoreResult score(FunctionalEvaluationStatus functional, int functionalPoints,
            Integer securityPoints, SecurityCorrectnessStatus security, Integer understandingPoints,
            UnderstandingAssessmentStatus understanding, Integer independentPoints) {
        return service.calculate(functional, functionalPoints, securityPoints, security, understandingPoints,
                understanding, independentPoints);
    }
}

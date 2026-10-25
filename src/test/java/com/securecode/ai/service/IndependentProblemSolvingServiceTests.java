package com.securecode.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.Difficulty;
import com.securecode.ai.entity.HintAssistanceLevel;
import com.securecode.ai.entity.HintViewEvent;
import com.securecode.ai.entity.Role;
import com.securecode.ai.entity.User;
import com.securecode.ai.repository.HintViewEventRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class IndependentProblemSolvingServiceTests {
    private final HintViewEventRepository views = mock(HintViewEventRepository.class);
    private final IndependentProblemSolvingService service = new IndependentProblemSolvingService(views);

    @Test
    void noViewedHintsMeansIndependentScore() {
        when(views.findByUser_IdAndChallenge_Id(1L, 2L)).thenReturn(List.of());
        assertThat(service.score(1L, 2L)).isEqualTo(new IndependentProblemSolvingResult(15, null));
    }

    @Test
    void conceptualHintScoresTenPoints() {
        when(views.findByUser_IdAndChallenge_Id(1L, 2L)).thenReturn(List.of(view(HintAssistanceLevel.CONCEPTUAL)));
        assertThat(service.score(1L, 2L)).isEqualTo(new IndependentProblemSolvingResult(10, HintAssistanceLevel.CONCEPTUAL));
    }

    @Test
    void implementationHintScoresFivePoints() {
        when(views.findByUser_IdAndChallenge_Id(1L, 2L)).thenReturn(List.of(view(HintAssistanceLevel.IMPLEMENTATION)));
        assertThat(service.score(1L, 2L)).isEqualTo(new IndependentProblemSolvingResult(5, HintAssistanceLevel.IMPLEMENTATION));
    }

    @Test
    void solutionRevealScoresZeroPoints() {
        when(views.findByUser_IdAndChallenge_Id(1L, 2L)).thenReturn(List.of(view(HintAssistanceLevel.SOLUTION)));
        assertThat(service.score(1L, 2L)).isEqualTo(new IndependentProblemSolvingResult(0, HintAssistanceLevel.SOLUTION));
    }

    @Test
    void multipleHintsUseTheHighestAssistanceLevel() {
        when(views.findByUser_IdAndChallenge_Id(1L, 2L)).thenReturn(List.of(view(HintAssistanceLevel.CONCEPTUAL),
                view(HintAssistanceLevel.IMPLEMENTATION), view(HintAssistanceLevel.SOLUTION)));
        assertThat(service.score(1L, 2L)).isEqualTo(new IndependentProblemSolvingResult(0, HintAssistanceLevel.SOLUTION));
    }

    private HintViewEvent view(HintAssistanceLevel level) {
        return new HintViewEvent(new User("student@example.com", "unused", Role.STUDENT),
                new Challenge(1, "SQL", "Use parameters", "java", "Injection", "SQL Injection", "CWE-89", "", Difficulty.EASY), level);
    }
}

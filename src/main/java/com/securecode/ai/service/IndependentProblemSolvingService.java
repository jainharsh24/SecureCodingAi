package com.securecode.ai.service;

import com.securecode.ai.entity.HintAssistanceLevel;
import com.securecode.ai.entity.HintViewEvent;
import com.securecode.ai.repository.HintViewEventRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/** Computes the Step 4 component from persisted views, never from code correctness. */
@Service
public class IndependentProblemSolvingService {
    private final HintViewEventRepository views;
    public IndependentProblemSolvingService(HintViewEventRepository views) { this.views = views; }
    public IndependentProblemSolvingResult score(Long userId, Long challengeId) {
        List<HintViewEvent> events = views.findByUser_IdAndChallenge_Id(userId, challengeId);
        HintAssistanceLevel highestAssistance = events.stream().map(HintViewEvent::getAssistanceLevel)
                .max(java.util.Comparator.comparingInt(this::assistanceRank)).orElse(null);
        return highestAssistance == null ? new IndependentProblemSolvingResult(15, null)
                : new IndependentProblemSolvingResult(highestAssistance.independentProblemSolvingScore(), highestAssistance);
    }
    private int assistanceRank(HintAssistanceLevel level) { return switch (level) {
        case CONCEPTUAL -> 1; case IMPLEMENTATION -> 2; case SOLUTION -> 3;
    }; }
}

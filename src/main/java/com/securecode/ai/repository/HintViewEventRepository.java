package com.securecode.ai.repository;

import com.securecode.ai.entity.HintAssistanceLevel;
import com.securecode.ai.entity.HintViewEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HintViewEventRepository extends JpaRepository<HintViewEvent, Long> {
    List<HintViewEvent> findByUser_IdAndChallenge_Id(Long userId, Long challengeId);
    Optional<HintViewEvent> findByUser_IdAndChallenge_IdAndAssistanceLevel(Long userId, Long challengeId, HintAssistanceLevel assistanceLevel);
}

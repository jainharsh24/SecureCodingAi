package com.securecode.ai.repository;

import com.securecode.ai.entity.UnderstandingAssessment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnderstandingAssessmentRepository extends JpaRepository<UnderstandingAssessment, Long> {
    List<UnderstandingAssessment> findByUser_IdAndChallenge_IdOrderByIdAsc(Long userId, Long challengeId);
    Optional<UnderstandingAssessment> findFirstByUser_IdAndChallenge_IdOrderByIdDesc(Long userId, Long challengeId);
    Optional<UnderstandingAssessment> findFirstByUser_IdAndSubmission_IdOrderByIdDesc(Long userId, Long submissionId);
}

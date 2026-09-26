package com.securecode.ai.repository;
import com.securecode.ai.entity.FunctionalEvaluation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FunctionalEvaluationRepository extends JpaRepository<FunctionalEvaluation, Long> {
    Optional<FunctionalEvaluation> findBySubmission_Id(Long submissionId);
}

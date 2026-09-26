package com.securecode.ai.repository;

import com.securecode.ai.entity.SecurityEvaluation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityEvaluationRepository extends JpaRepository<SecurityEvaluation, Long> {
    List<SecurityEvaluation> findBySubmission_Id(Long submissionId);
}

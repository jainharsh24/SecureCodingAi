package com.securecode.ai.repository;
import com.securecode.ai.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    long countByUser_IdAndChallenge_Id(Long userId, Long challengeId);
    List<Submission> findByUser_EmailOrderBySubmittedAtDesc(String email);
    List<Submission> findByUser_EmailAndChallenge_IdOrderByAttemptNumberAsc(String email, Long challengeId);
}

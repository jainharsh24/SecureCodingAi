package com.securecode.ai.repository;
import com.securecode.ai.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SubmissionRepository extends JpaRepository<Submission, Long> { }

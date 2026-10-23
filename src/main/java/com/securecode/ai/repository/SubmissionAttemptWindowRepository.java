package com.securecode.ai.repository;

import com.securecode.ai.entity.SubmissionAttemptWindow;
import com.securecode.ai.entity.SubmissionAttemptWindowId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface SubmissionAttemptWindowRepository extends JpaRepository<SubmissionAttemptWindow, SubmissionAttemptWindowId> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SubmissionAttemptWindow> findWithLockById(SubmissionAttemptWindowId id);
}

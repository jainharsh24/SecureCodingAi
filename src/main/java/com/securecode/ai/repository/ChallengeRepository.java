package com.securecode.ai.repository;

import com.securecode.ai.entity.Challenge;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    @Override @EntityGraph(attributePaths = "testCases") List<Challenge> findAll();
    @EntityGraph(attributePaths = {"testCases", "prerequisites"}) List<Challenge> findByActiveTrueOrderByChallengeNumberAsc();
    @Override @EntityGraph(attributePaths = "testCases") Optional<Challenge> findById(Long id);
    @Query(value = "select nextval('challenge_number_sequence')", nativeQuery = true)
    long nextChallengeNumber();
}

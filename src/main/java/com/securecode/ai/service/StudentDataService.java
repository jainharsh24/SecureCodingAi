package com.securecode.ai.service;

import com.securecode.ai.dto.SubmissionHistoryResponse;
import com.securecode.ai.dto.LearningProgressPoint;
import com.securecode.ai.dto.StudentAnalyticsResponse;
import com.securecode.ai.dto.UserSummaryResponse;
import com.securecode.ai.dto.VulnerabilitySubtypeProgress;
import com.securecode.ai.entity.SecurityEvaluation;
import com.securecode.ai.entity.Submission;
import com.securecode.ai.entity.User;
import com.securecode.ai.repository.FunctionalEvaluationRepository;
import com.securecode.ai.repository.SecurityEvaluationRepository;
import com.securecode.ai.repository.SubmissionRepository;
import com.securecode.ai.repository.UserRepository;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** User-scoped history and aggregate read models. No browser state is used for these values. */
@Service
public class StudentDataService {
    private final UserRepository users;
    private final SubmissionRepository submissions;
    private final FunctionalEvaluationRepository functionals;
    private final SecurityEvaluationRepository securities;

    public StudentDataService(UserRepository users, SubmissionRepository submissions, FunctionalEvaluationRepository functionals, SecurityEvaluationRepository securities) {
        this.users = users; this.submissions = submissions; this.functionals = functionals; this.securities = securities;
    }

    @Transactional(readOnly = true)
    public List<SubmissionHistoryResponse> history(String email) {
        user(email);
        return submissions.findByUser_EmailOrderBySubmittedAtDesc(email).stream().map(this::historyItem).toList();
    }

    @Transactional(readOnly = true)
    public UserSummaryResponse summary(String email) {
        User user = user(email);
        List<Submission> all = submissions.findByUser_EmailOrderBySubmittedAtDesc(email);
        long attempted = all.stream().map(s -> s.getChallenge().getId()).distinct().count();
        long solved = all.stream().filter(s -> s.getLearningScore() >= 90).map(s -> s.getChallenge().getId()).distinct().count();
        double average = all.isEmpty() ? 0 : Math.round(all.stream().mapToInt(Submission::getLearningScore).average().orElse(0) * 10.0) / 10.0;
        return new UserSummaryResponse(user.getName(), user.getEmail(), solved, attempted, all.size(), average);
    }

    @Transactional(readOnly = true)
    public StudentAnalyticsResponse analytics(String email) {
        user(email);
        List<Submission> all = submissions.findByUser_EmailOrderBySubmittedAtDesc(email);
        List<LearningProgressPoint> progress = all.reversed().stream()
                .map(s -> new LearningProgressPoint(s.getChallenge().getTitle(), s.getAttemptNumber(), s.getLearningScore(), s.getSubmittedAt()))
                .toList();
        long passed = all.stream().filter(s -> functionals.findBySubmission_Id(s.getId())
                .map(e -> "PASS".equals(e.getStatus().name())).orElse(false)).count();
        int highestSafeEvidence = all.stream().mapToInt(this::safeEvidencePercent).max().orElse(0);
        Map<Long, String> solvedChallenges = new LinkedHashMap<>();
        all.stream().filter(s -> s.getLearningScore() >= 90).forEach(s -> solvedChallenges.putIfAbsent(s.getChallenge().getId(), subtype(s)));
        List<VulnerabilitySubtypeProgress> bySubtype = solvedChallenges.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(value -> value, LinkedHashMap::new, java.util.stream.Collectors.counting()))
                .entrySet().stream().map(entry -> new VulnerabilitySubtypeProgress(entry.getKey(), entry.getValue())).toList();
        return new StudentAnalyticsResponse(solvedChallenges.size(), passed, highestSafeEvidence, progress, bySubtype);
    }

    private SubmissionHistoryResponse historyItem(Submission submission) {
        List<SecurityEvaluation> evidence = securities.findBySubmission_Id(submission.getId());
        int completed = (int) evidence.stream().filter(e -> "COMPLETED".equals(e.getEvaluatorStatus())).count();
        int safe = (int) evidence.stream().filter(e -> "COMPLETED".equals(e.getEvaluatorStatus()) && !e.isDetected()).count();
        int safePercent = completed == 0 ? 0 : Math.round(safe * 100f / completed);
        String functional = functionals.findBySubmission_Id(submission.getId()).map(e -> e.getStatus().name()).orElse("NOT_AVAILABLE");
        return new SubmissionHistoryResponse(submission.getId(), submission.getChallenge().getId(), submission.getChallenge().getTitle(),
                submission.getChallenge().getDifficulty().name(), submission.getAttemptNumber(), submission.getSubmittedAt(), functional,
                submission.getRawAssessment(), submission.getLearningScore(), safePercent, completed == 0 ? 0 : 100 - safePercent,
                historyEvidenceState(functional, submission.getRawAssessment(), completed));
    }

    private int safeEvidencePercent(Submission submission) {
        List<SecurityEvaluation> evidence = securities.findBySubmission_Id(submission.getId());
        int completed = (int) evidence.stream().filter(e -> "COMPLETED".equals(e.getEvaluatorStatus())).count();
        int safe = (int) evidence.stream().filter(e -> "COMPLETED".equals(e.getEvaluatorStatus()) && !e.isDetected()).count();
        return completed == 0 ? 0 : Math.round(safe * 100f / completed);
    }

    private String subtype(Submission submission) {
        String subtype = submission.getChallenge().getVulnerabilitySubtype();
        return subtype == null || subtype.isBlank() ? "Unspecified" : subtype;
    }

    private String historyEvidenceState(String functional, String assessment, int completedEvidence) {
        if (!"PASS".equals(functional)) return "NOT_AVAILABLE";
        if (completedEvidence == 0) return "NO SECURITY EVIDENCE";
        if ("STRONG SAFE".equals(assessment)) return "STRONG SAFE EVIDENCE";
        if ("STRONG VULNERABILITY".equals(assessment)) return "STRONG VULNERABILITY EVIDENCE";
        if ("MOSTLY SAFE".equals(assessment)) return "MOSTLY SAFE — CONFLICT EXISTS";
        return assessment == null || assessment.isBlank() ? "CONFLICTING EVIDENCE" : assessment;
    }

    private User user(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }
}

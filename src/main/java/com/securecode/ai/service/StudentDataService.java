package com.securecode.ai.service;

import com.securecode.ai.dto.SubmissionHistoryResponse;
import com.securecode.ai.dto.LearningProgressPoint;
import com.securecode.ai.dto.StudentAnalyticsResponse;
import com.securecode.ai.dto.UserSummaryResponse;
import com.securecode.ai.dto.VulnerabilitySubtypeProgress;
import com.securecode.ai.dto.DifficultyAccuracyResponse;
import com.securecode.ai.dto.AnalyticsTrendPoint;
import com.securecode.ai.dto.ChallengeProgressResponse;
import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.Difficulty;
import com.securecode.ai.entity.FunctionalEvaluation;
import com.securecode.ai.entity.FunctionalEvaluationStatus;
import com.securecode.ai.entity.SecurityEvaluation;
import com.securecode.ai.entity.Submission;
import com.securecode.ai.entity.User;
import com.securecode.ai.repository.FunctionalEvaluationRepository;
import com.securecode.ai.repository.SecurityEvaluationRepository;
import com.securecode.ai.repository.SubmissionRepository;
import com.securecode.ai.repository.UserRepository;
import com.securecode.ai.repository.ChallengeRepository;
import java.time.ZoneOffset;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    private final ChallengeRepository challenges;

    public StudentDataService(UserRepository users, SubmissionRepository submissions, FunctionalEvaluationRepository functionals, SecurityEvaluationRepository securities) {
        this(users, submissions, functionals, securities, null);
    }
    @org.springframework.beans.factory.annotation.Autowired
    public StudentDataService(UserRepository users, SubmissionRepository submissions, FunctionalEvaluationRepository functionals, SecurityEvaluationRepository securities, ChallengeRepository challenges) {
        this.users = users; this.submissions = submissions; this.functionals = functionals; this.securities = securities; this.challenges = challenges;
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
        List<FunctionalEvaluation> evaluationList = functionals.findBySubmission_User_Email(email);
        Map<Long, FunctionalEvaluationStatus> functionalBySubmission = evaluationList.stream()
                .collect(Collectors.toMap(FunctionalEvaluation::getSubmissionId, FunctionalEvaluation::getStatus, (left, right) -> left));
        List<LearningProgressPoint> progress = all.reversed().stream()
                .map(s -> new LearningProgressPoint(s.getChallenge().getChallengeNumber(), s.getChallenge().getTitle(), s.getAttemptNumber(), s.getLearningScore(), s.getSubmittedAt()))
                .toList();
        long passed = all.stream().filter(s -> functionalBySubmission.get(s.getId()) == FunctionalEvaluationStatus.PASS).count();
        int highestSafeEvidence = all.stream().mapToInt(this::safeEvidencePercent).max().orElse(0);
        Map<Long, String> solvedChallenges = new LinkedHashMap<>();
        all.stream().filter(s -> s.getLearningScore() >= 90).forEach(s -> solvedChallenges.putIfAbsent(s.getChallenge().getId(), subtype(s)));
        List<VulnerabilitySubtypeProgress> bySubtype = solvedChallenges.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(value -> value, LinkedHashMap::new, java.util.stream.Collectors.counting()))
                .entrySet().stream().map(entry -> new VulnerabilitySubtypeProgress(entry.getKey(), entry.getValue())).toList();
        if (challenges == null) return new StudentAnalyticsResponse(solvedChallenges.size(), passed, highestSafeEvidence, progress, bySubtype,
                0, 0, 0, 0, List.of(), List.of(), List.of(), List.of());
        List<Challenge> active = challenges.findByActiveTrueOrderByChallengeNumberAsc();
        Map<Long, List<Submission>> byChallenge = all.stream().collect(Collectors.groupingBy(s -> s.getChallenge().getId()));
        Set<Long> solvedIds = all.stream().filter(s -> s.getLearningScore() >= 90).map(s -> s.getChallenge().getId()).collect(Collectors.toSet());
        List<ChallengeProgressResponse> perChallenge = active.stream().map(challenge -> challengeProgress(challenge, byChallenge.getOrDefault(challenge.getId(), List.of()), solvedIds, functionalBySubmission, active)).toList();
        List<DifficultyAccuracyResponse> accuracy = java.util.Arrays.stream(Difficulty.values()).map(difficulty -> accuracy(difficulty, all, functionalBySubmission)).toList();
        List<AnalyticsTrendPoint> weekly = trends(all, functionalBySubmission, solvedIds, true);
        List<AnalyticsTrendPoint> monthly = trends(all, functionalBySubmission, solvedIds, false);
        int completion = active.isEmpty() ? 0 : Math.round(solvedIds.stream().filter(id -> active.stream().anyMatch(c -> c.getId().equals(id))).count() * 100f / active.size());
        long attempted = byChallenge.keySet().stream().filter(id -> active.stream().anyMatch(c -> c.getId().equals(id))).count();
        return new StudentAnalyticsResponse(solvedChallenges.size(), passed, highestSafeEvidence, progress, bySubtype, active.size(), attempted,
                active.size() - solvedIds.stream().filter(id -> active.stream().anyMatch(c -> c.getId().equals(id))).count(), completion,
                accuracy, weekly, monthly, perChallenge);
    }

    private ChallengeProgressResponse challengeProgress(Challenge challenge, List<Submission> attempts, Set<Long> solved,
            Map<Long, FunctionalEvaluationStatus> results, List<Challenge> active) {
        int prerequisiteCount = challenge.getPrerequisites().size();
        int completePrerequisites = (int) challenge.getPrerequisites().stream().filter(c -> solved.contains(c.getId())).count();
        boolean locked = prerequisiteCount > completePrerequisites;
        Submission latest = attempts.stream().max(Comparator.comparing(Submission::getSubmittedAt, Comparator.nullsLast(Comparator.naturalOrder()))).orElse(null);
        int best = attempts.stream().mapToInt(Submission::getLearningScore).max().orElse(0);
        String status = solved.contains(challenge.getId()) ? "SOLVED" : attempts.isEmpty() ? "NOT_STARTED" : "ATTEMPTED";
        String latestResult = latest == null ? "NOT_EVALUATED" : results.getOrDefault(latest.getId(), FunctionalEvaluationStatus.EVALUATION_ERROR).name();
        return new ChallengeProgressResponse(challenge.getId(), active.indexOf(challenge) + 1, challenge.getTitle(), challenge.getDifficulty().name(),
                subtype(challenge), status, locked, completePrerequisites, prerequisiteCount, attempts.size(), latestResult, best);
    }

    private DifficultyAccuracyResponse accuracy(Difficulty difficulty, List<Submission> submissions,
            Map<Long, FunctionalEvaluationStatus> results) {
        List<FunctionalEvaluationStatus> evaluated = submissions.stream().filter(s -> s.getChallenge().getDifficulty() == difficulty)
                .map(s -> results.get(s.getId())).filter(status -> status == FunctionalEvaluationStatus.PASS || status == FunctionalEvaluationStatus.FAIL).toList();
        long successes = evaluated.stream().filter(status -> status == FunctionalEvaluationStatus.PASS).count();
        return new DifficultyAccuracyResponse(difficulty.name(), successes, evaluated.size(), evaluated.isEmpty() ? 0 : Math.round(successes * 100f / evaluated.size()));
    }

    private List<AnalyticsTrendPoint> trends(List<Submission> submissions, Map<Long, FunctionalEvaluationStatus> results,
            Set<Long> solved, boolean weekly) {
        Map<String, List<Submission>> buckets = submissions.stream().filter(s -> s.getSubmittedAt() != null).collect(Collectors.groupingBy(s -> bucket(s, weekly)));
        Set<Long> countedSolved = new HashSet<>();
        return buckets.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> {
            List<Submission> attempts = entry.getValue();
            long evaluated = attempts.stream().filter(s -> evaluated(results.get(s.getId()))).count();
            long successful = attempts.stream().filter(s -> results.get(s.getId()) == FunctionalEvaluationStatus.PASS).count();
            long newlySolved = attempts.stream().sorted(Comparator.comparing(Submission::getSubmittedAt)).filter(s -> solved.contains(s.getChallenge().getId()) && countedSolved.add(s.getChallenge().getId())).count();
            return new AnalyticsTrendPoint(entry.getKey(), evaluated, successful, newlySolved, evaluated == 0 ? 0 : Math.round(successful * 100f / evaluated));
        }).toList();
    }
    private boolean evaluated(FunctionalEvaluationStatus status) { return status == FunctionalEvaluationStatus.PASS || status == FunctionalEvaluationStatus.FAIL; }
    private String bucket(Submission submission, boolean weekly) { var date = submission.getSubmittedAt().atZone(ZoneOffset.UTC); return weekly ? String.format("%d-W%02d", date.get(WeekFields.ISO.weekBasedYear()), date.get(WeekFields.ISO.weekOfWeekBasedYear())) : YearMonth.from(date).toString(); }
    private String subtype(Challenge challenge) { String value = challenge.getVulnerabilitySubtype(); return value == null || value.isBlank() ? "Unspecified" : value; }

    private SubmissionHistoryResponse historyItem(Submission submission) {
        List<SecurityEvaluation> evidence = securities.findBySubmission_Id(submission.getId());
        int completed = (int) evidence.stream().filter(e -> "COMPLETED".equals(e.getEvaluatorStatus())).count();
        int safe = (int) evidence.stream().filter(e -> "COMPLETED".equals(e.getEvaluatorStatus()) && !e.isDetected()).count();
        int safePercent = completed == 0 ? 0 : Math.round(safe * 100f / completed);
        String functional = functionals.findBySubmission_Id(submission.getId()).map(e -> e.getStatus().name()).orElse("NOT_AVAILABLE");
        return new SubmissionHistoryResponse(submission.getId(), submission.getChallenge().getId(), submission.getChallenge().getChallengeNumber(), submission.getChallenge().getTitle(),
                submission.getChallenge().getDifficulty().name(), submission.getAttemptNumber(), submission.getSubmittedAt(), functional,
                submission.getRawAssessment(), submission.getLearningScore(), safePercent, completed == 0 ? 0 : 100 - safePercent,
                historyEvidenceState(functional, evidence));
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

    private String historyEvidenceState(String functional, List<SecurityEvaluation> evidence) {
        if ("FAIL".equals(functional)) return "Functional Fail";
        if ("STARTER_CODE".equals(functional)) return "INCOMPLETE ATTEMPT";
        if (!"PASS".equals(functional)) return "NOT_AVAILABLE";
        int completedEvidence = (int) evidence.stream().filter(e -> "COMPLETED".equals(e.getEvaluatorStatus())).count();
        if (completedEvidence == 0) return "NO SECURITY EVIDENCE";
        int safe = (int) evidence.stream().filter(e -> "COMPLETED".equals(e.getEvaluatorStatus()) && !e.isDetected()).count();
        int vulnerable = completedEvidence - safe;
        if (vulnerable == 0) return "STRONG SAFE EVIDENCE";
        if (safe == 0) return "STRONG VULNERABILITY EVIDENCE";
        if (safe > vulnerable) return "MOSTLY SAFE — CONFLICT EXISTS";
        if (vulnerable > safe) return "MOSTLY VULNERABLE — CONFLICT EXISTS";
        return "CONFLICTING EVIDENCE";
    }

    private User user(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }
}

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
import com.securecode.ai.evaluation.common.SecurityEvidenceSummary;
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
    private static final List<String> ANALYTICS_SUBTYPES = List.of(
            "SQL Injection", "NoSQL Injection", "Command Injection", "LDAP Injection",
            "XPath Injection", "XML Injection", "CRLF Injection", "Template/SSTI Injection",
            "Code Injection", "Email Header Injection", "XXE Injection", "GraphQL Injection");
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
        Map<Long, Integer> catalogNumbers = catalogNumbers();
        return submissions.findByUser_EmailOrderBySubmittedAtDesc(email).stream().map(submission -> historyItem(submission, catalogNumbers)).toList();
    }

    @Transactional(readOnly = true)
    public List<SubmissionHistoryResponse> challengeAttempts(String email, Long challengeId) {
        user(email);
        Map<Long, Integer> catalogNumbers = catalogNumbers();
        return submissions.findByUser_EmailAndChallenge_IdOrderByAttemptNumberAsc(email, challengeId).stream()
                .sorted(Comparator.comparing(Submission::getSubmittedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(5).map(submission -> historyItem(submission, catalogNumbers)).toList();
    }

    @Transactional(readOnly = true)
    public UserSummaryResponse summary(String email) {
        User user = user(email);
        List<Submission> all = submissions.findByUser_EmailOrderBySubmittedAtDesc(email);
        long attempted = all.stream().map(s -> s.getChallenge().getId()).distinct().count();
        long solved = all.stream().filter(this::completed).map(s -> s.getChallenge().getId()).distinct().count();
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
        all.stream().filter(this::completed).forEach(s -> solvedChallenges.putIfAbsent(s.getChallenge().getId(), analyticsSubtype(s)));
        List<VulnerabilitySubtypeProgress> solvedBySubtype = solvedChallenges.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(value -> value, LinkedHashMap::new, java.util.stream.Collectors.counting()))
                .entrySet().stream().map(entry -> new VulnerabilitySubtypeProgress(entry.getKey(), entry.getValue())).toList();
        if (challenges == null) return new StudentAnalyticsResponse(solvedChallenges.size(), passed, highestSafeEvidence, progress, solvedBySubtype,
                0, 0, 0, 0, List.of(), List.of(), List.of(), List.of());
        List<Challenge> active = challenges.findByActiveTrueOrderByChallengeNumberAsc();
        Map<String, Long> subtypeCounts = solvedBySubtype.stream().collect(Collectors.toMap(
                VulnerabilitySubtypeProgress::vulnerabilitySubtype, VulnerabilitySubtypeProgress::questionsSolved));
        List<VulnerabilitySubtypeProgress> bySubtype = ANALYTICS_SUBTYPES.stream()
                .map(subtype -> new VulnerabilitySubtypeProgress(subtype, subtypeCounts.getOrDefault(subtype, 0L))).toList();
        Map<Long, List<Submission>> byChallenge = all.stream().collect(Collectors.groupingBy(s -> s.getChallenge().getId()));
        Set<Long> solvedIds = all.stream().filter(this::completed).map(s -> s.getChallenge().getId()).collect(Collectors.toSet());
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
    /** Old rows remain historical only; do not certify them as Step-5 verified completion. */
    private boolean completed(Submission submission) { return submission.isVerifiedCompleted(); }
    private String bucket(Submission submission, boolean weekly) { var date = submission.getSubmittedAt().atZone(ZoneOffset.UTC); return weekly ? String.format("%d-W%02d", date.get(WeekFields.ISO.weekBasedYear()), date.get(WeekFields.ISO.weekOfWeekBasedYear())) : YearMonth.from(date).toString(); }
    private String subtype(Challenge challenge) { String value = challenge.getVulnerabilitySubtype(); return value == null || value.isBlank() ? "Unspecified" : value; }

    private Map<Long, Integer> catalogNumbers() {
        if (challenges == null) return Map.of();
        List<Challenge> active = challenges.findByActiveTrueOrderByChallengeNumberAsc();
        Map<Long, Integer> numbers = new java.util.HashMap<>();
        for (int index = 0; index < active.size(); index++) numbers.put(active.get(index).getId(), index + 1);
        return numbers;
    }

    private SubmissionHistoryResponse historyItem(Submission submission, Map<Long, Integer> catalogNumbers) {
        List<SecurityEvaluation> evidence = securities.findBySubmission_Id(submission.getId());
        SecurityEvidenceSummary summary = SecurityEvidenceSummary.from(evidence);
        String functional = functionals.findBySubmission_Id(submission.getId()).map(e -> e.getStatus().name()).orElse("NOT_AVAILABLE");
        return new SubmissionHistoryResponse(submission.getId(), submission.getChallenge().getId(), submission.getChallenge().getChallengeNumber(),
                catalogNumbers.get(submission.getChallenge().getId()), submission.getChallenge().getTitle(),
                submission.getChallenge().getDifficulty().name(), submission.getAttemptNumber(), submission.getSubmittedAt(), functional,
                submission.getRawAssessment(), submission.getLearningScore(), summary.safePercent(), summary.vulnerabilityPercent(),
                historyEvidenceState(submission, functional, evidence), submission.isVerifiedCompleted());
    }

    private int safeEvidencePercent(Submission submission) {
        return SecurityEvidenceSummary.from(securities.findBySubmission_Id(submission.getId())).safePercent();
    }

    private String subtype(Submission submission) {
        String subtype = submission.getChallenge().getVulnerabilitySubtype();
        return subtype == null || subtype.isBlank() ? "Unspecified" : subtype;
    }

    /** Maps prerequisite and progression labels back to the twelve catalog vulnerability subtypes for comparison. */
    private String analyticsSubtype(Submission submission) {
        String value = subtype(submission).toLowerCase(java.util.Locale.ROOT);
        if (value.contains("graphql")) return "GraphQL Injection";
        if (value.contains("email")) return "Email Header Injection";
        if (value.contains("template") || value.contains("ssti")) return "Template/SSTI Injection";
        if (value.contains("command")) return "Command Injection";
        if (value.contains("nosql")) return "NoSQL Injection";
        if (value.contains("sql")) return "SQL Injection";
        if (value.contains("ldap")) return "LDAP Injection";
        if (value.contains("xpath")) return "XPath Injection";
        if (value.contains("xxe")) return "XXE Injection";
        if (value.contains("xml")) return "XML Injection";
        if (value.contains("crlf") || value.contains("header")) return "CRLF Injection";
        if (value.contains("code")) return "Code Injection";
        return subtype(submission);
    }

    private String historyEvidenceState(Submission submission, String functional, List<SecurityEvaluation> evidence) {
        if ("FAIL".equals(functional)) return "Functional Fail";
        if ("STARTER_CODE".equals(functional)) return "INCOMPLETE ATTEMPT";
        if (!"PASS".equals(functional)) return "NOT_AVAILABLE";
        SecurityEvidenceSummary summary = SecurityEvidenceSummary.from(evidence);
        if (summary.validEvaluatorCount() == 0) return "UNVERIFIED";
        String securityStatus = submission.getSecurityCorrectnessStatus();
        if ("TARGETED_VULNERABILITY".equals(securityStatus)) return "TARGETED VULNERABILITY";
        if ("CONFLICTING_EVIDENCE".equals(securityStatus)) return "CONFLICTING EVIDENCE";
        if ("VERIFIED_SAFE".equals(securityStatus)) return "VERIFIED SAFE";
        // Older rows predate the persisted correctness gate.  They remain useful only
        // when every recorded evaluator explicitly completed without an error; a missing,
        // timed-out, or failed evaluator is never summarized as strong-safe evidence.
        boolean legacyOutcomeIsVerified = securityStatus == null && !evidence.isEmpty()
                && evidence.stream().allMatch(e -> "COMPLETED".equals(e.getEvaluatorStatus())
                        && (e.getEvaluatorError() == null || e.getEvaluatorError().isBlank()));
        if ((securityStatus == null && !legacyOutcomeIsVerified)
                || "UNVERIFIED".equals(securityStatus) || "NOT_RUN".equals(securityStatus)) {
            return "UNVERIFIED";
        }
        int safe = summary.safeCount();
        int vulnerable = summary.vulnerableCount();
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

package com.securecode.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.securecode.ai.entity.Submission;
import com.securecode.ai.repository.ChallengeRepository;
import com.securecode.ai.repository.SubmissionRepository;
import com.securecode.ai.service.SubmissionAnalysisService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CatalogPreservationIntegrationTests {
    private static final List<String> ACTIVE_TYPES = List.of(
            "SQL Injection", "NoSQL Injection", "Command Injection", "LDAP Injection",
            "XPath Injection", "XML Injection", "CRLF Injection", "Template/SSTI Injection",
            "Code Injection", "Email Header Injection", "XXE Injection", "GraphQL Injection");

    @Autowired private ChallengeRepository challenges;
    @Autowired private SubmissionRepository submissions;
    @Autowired private SubmissionAnalysisService analysis;
    @TempDir Path temporaryDirectory;

    @Test
    void exposesOnlyTheTwelveActiveInjectionChallenges() {
        var active = challenges.findByActiveTrueOrderByChallengeNumberAsc();

        assertThat(active).hasSize(12);
        assertThat(active).allSatisfy(challenge -> {
            assertThat(challenge.getDescription()).isNotBlank();
            assertThat(challenge.getStarterCode()).isNotBlank();
            assertThat(challenge.getSecureReferenceCode()).isNotBlank();
            assertThat(challenge.getTestCases()).isNotEmpty();
        });
        assertThat(active).extracting(challenge -> challenge.getVulnerabilitySubtype())
                .containsExactlyInAnyOrderElementsOf(ACTIVE_TYPES);
    }

    @Test
    void secureReferenceSolutionsCompileAndPassTheirConfiguredCases() throws Exception {
        for (var challenge : challenges.findByActiveTrueOrderByChallengeNumberAsc()) {
            Path challengeDirectory = Files.createDirectory(temporaryDirectory.resolve("challenge-" + challenge.getId()));
            Path source = challengeDirectory.resolve("Main.java");
            Files.writeString(source, challenge.getSecureReferenceCode());
            int compilation = ToolProvider.getSystemJavaCompiler().run(null, null, null,
                    "-d", challengeDirectory.toString(), source.toString());
            assertThat(compilation).as(challenge.getTitle()).isZero();
            for (var testCase : challenge.getTestCases()) {
                Process process = new ProcessBuilder(javaExecutable(), "-cp", challengeDirectory.toString(), "Main")
                        .redirectErrorStream(true).start();
                try (var input = process.getOutputStream()) {
                    input.write(testCase.getInputData().getBytes());
                    input.write(System.lineSeparator().getBytes());
                }
                String output = new String(process.getInputStream().readAllBytes()).trim();
                assertThat(process.waitFor()).as(challenge.getTitle()).isZero();
                assertThat(output).as(challenge.getTitle() + " case " + testCase.getPosition())
                        .isEqualTo(testCase.getExpectedOutput());
            }
        }
    }

    private String javaExecutable() {
        return Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
    }

    @Test
    void historicalSubmissionAnalysisUsesItsOriginalInactiveChallenge() {
        Submission historicalSubmission = submissions.findAll().stream()
                .filter(submission -> !submission.getChallenge().isActive())
                .findFirst()
                .orElseThrow();

        var response = analysis.getForStudent(historicalSubmission.getId(), historicalSubmission.getUser().getEmail());

        assertThat(response.submissionContext().challengeTitle()).isEqualTo(historicalSubmission.getChallenge().getTitle());
        assertThat(response.submissionContext().challengeDescription()).isEqualTo(historicalSubmission.getChallenge().getDescription());
        assertThat(response.submissionContext().submittedCode()).isEqualTo(historicalSubmission.getSourceCode());
    }
}

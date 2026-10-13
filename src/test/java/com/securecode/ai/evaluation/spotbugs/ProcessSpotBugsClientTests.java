package com.securecode.ai.evaluation.spotbugs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.securecode.ai.evaluation.spotbugs.config.SpotBugsProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

class ProcessSpotBugsClientTests {
    private final ProcessSpotBugsClient client = new ProcessSpotBugsClient(
            new SpotBugsProperties(true, "spotbugs.bat", "C:/findsecbugs-plugin.jar", "javac", 30_000));

    @Test
    void keepsOnlySecurityFindingsAndExtractsTypeCweAndSourceLine() throws Exception {
        Path report = Files.createTempFile("spotbugs-report", ".xml");
        try {
            Files.writeString(report, """
                    <?xml version="1.0"?>
                    <BugCollection>
                      <BugInstance type="SQL_INJECTION_JDBC" category="SECURITY" priority="1" cweid="89">
                        <LongMessage>Potential SQL injection</LongMessage>
                        <SourceLine sourcepath="Main.java" start="3"/>
                      </BugInstance>
                      <BugInstance type="DMI_CONSTANT_DB_PASSWORD" category="BAD_PRACTICE" priority="2">
                        <LongMessage>Ignored because it is not SECURITY</LongMessage>
                        <SourceLine sourcepath="Main.java" start="1"/>
                      </BugInstance>
                    </BugCollection>
                    """);
            var findings = client.parseSecurityFindings(report, "class Main {\n  void run() {}\n  void query() {}\n}");

            assertEquals(1, findings.size());
            assertEquals("SQL_INJECTION_JDBC", findings.getFirst().vulnerabilityType());
            assertEquals("CWE-89", findings.getFirst().cwe());
            assertEquals(3, findings.getFirst().startLine());
            assertEquals("void query() {}", findings.getFirst().code());
        } finally {
            Files.deleteIfExists(report);
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "RUN_SPOTBUGS_INTEGRATION_TESTS", matches = "true")
    void runsWindowsSpotBugsForVulnerableAndPreparedStatementCode() {
        ProcessSpotBugsClient windowsClient = new ProcessSpotBugsClient(new SpotBugsProperties(true,
                "C:/Users/harsh/Downloads/spotbugs-4.10.4/bin/spotbugs.bat",
                "C:/Users/harsh/Downloads/spotbugs-4.10.4/plugin/findsecbugs-plugin-1.14.0.jar",
                "C:/Program Files/Java/jdk-25/bin/javac.exe", 30_000));

        var vulnerable = windowsClient.scanJava("""
                public class Main {
                    static void lookup(java.sql.Statement statement, String username) throws Exception {
                        String query = "SELECT * FROM users WHERE username = '" + username + "'";
                        statement.executeQuery(query);
                    }
                }
                """);
        assertEquals(com.securecode.ai.evaluation.SecurityEvaluationStatus.COMPLETED, vulnerable.status(), vulnerable.error());
        org.junit.jupiter.api.Assertions.assertTrue(vulnerable.findings().stream()
                .anyMatch(finding -> "SQL_INJECTION_JDBC".equals(finding.ruleId())));

        var preparedStatement = windowsClient.scanJava("""
                public class Main {
                    static void lookup(java.sql.PreparedStatement statement, String username) throws Exception {
                        statement.setString(1, username);
                        statement.executeQuery();
                    }
                }
                """);
        assertEquals(com.securecode.ai.evaluation.SecurityEvaluationStatus.COMPLETED, preparedStatement.status(), preparedStatement.error());
        org.junit.jupiter.api.Assertions.assertFalse(preparedStatement.findings().stream()
                .anyMatch(finding -> finding.ruleId().contains("SQL_INJECTION")));
    }
}

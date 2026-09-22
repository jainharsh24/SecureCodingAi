package com.securecode.ai.evaluation;

import com.securecode.ai.config.SemgrepProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ProcessSemgrepClient implements SemgrepClient {
    private final SemgrepProperties properties;
    private final JsonMapper jsonMapper;

    public ProcessSemgrepClient(SemgrepProperties properties, JsonMapper jsonMapper) {
        this.properties = properties;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public SemgrepScanResult scanJava(String sourceCode) {
        if (!properties.enabled()) {
            return new SemgrepScanResult(SecurityEvaluationStatus.SKIPPED, List.of(), null, "Semgrep is disabled");
        }

        Path temporaryDirectory = null;
        try {
            temporaryDirectory = Files.createTempDirectory("securecode-semgrep-");
            Path sourceFile = temporaryDirectory.resolve("Main.java");
            Files.writeString(sourceFile, sourceCode, StandardCharsets.UTF_8);
            String sourcePath = wslPath(sourceFile);
            String rulesPath = wslPath(Path.of(properties.rulesPath()).toAbsolutePath());
            Process process = wslLoginShell(properties.executable(), "scan", "--json", "--config", rulesPath, sourcePath);
            boolean completed = process.waitFor(properties.timeoutMs(), TimeUnit.MILLISECONDS);
            if (!completed) {
                process.destroyForcibly();
                return new SemgrepScanResult(SecurityEvaluationStatus.TIMEOUT, List.of(), null,
                        "Semgrep scan exceeded " + properties.timeoutMs() + " ms");
            }

            String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            try {
                List<SecurityFinding> findings = parseFindings(stdout, sourceCode);
                if (process.exitValue() != 0 && findings.isEmpty()) {
                    return new SemgrepScanResult(SecurityEvaluationStatus.ERROR, List.of(), stdout,
                            errorMessage(stderr, "Semgrep exited with code " + process.exitValue()));
                }
                return new SemgrepScanResult(SecurityEvaluationStatus.COMPLETED, findings, stdout, null);
            } catch (IOException exception) {
                return new SemgrepScanResult(SecurityEvaluationStatus.ERROR, List.of(), stdout,
                        errorMessage(stderr, "Semgrep returned invalid JSON: " + exception.getMessage()));
            }
        } catch (IOException exception) {
            return new SemgrepScanResult(SecurityEvaluationStatus.ERROR, List.of(), null,
                    "Could not start Semgrep through WSL: " + exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new SemgrepScanResult(SecurityEvaluationStatus.ERROR, List.of(), null, "Semgrep scan was interrupted");
        } finally {
            deleteTemporaryDirectory(temporaryDirectory);
        }
    }

    private String wslPath(Path path) throws IOException, InterruptedException {
        Process process = wslLoginShell("wslpath", "-a", path.toString());
        if (!process.waitFor(5, TimeUnit.SECONDS) || process.exitValue() != 0) {
            process.destroyForcibly();
            throw new IOException("Unable to translate a Windows path for WSL");
        }
        return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
    }

    private Process wslLoginShell(String... command) throws IOException {
        String shellCommand = java.util.Arrays.stream(command).map(this::shellQuote).collect(java.util.stream.Collectors.joining(" "));
        return new ProcessBuilder("wsl.exe", "-d", properties.wslDistribution(), "--", "bash", "-lc", shellCommand).start();
    }

    private String shellQuote(String value) { return "'" + value.replace("'", "'\\\"'\\\"'") + "'"; }

    private List<SecurityFinding> parseFindings(String json, String sourceCode) throws IOException {
        JsonNode results = jsonMapper.readTree(json).path("results");
        List<SecurityFinding> findings = new ArrayList<>();
        for (JsonNode result : results) {
            JsonNode extra = result.path("extra");
            JsonNode metadata = extra.path("metadata");
            JsonNode start = result.path("start");
            JsonNode end = result.path("end");
            Integer line = integerValue(start.path("line"));
            findings.add(new SecurityFinding(result.path("check_id").asText(), extra.path("message").asText(),
                    extra.path("severity").asText(), metadata.path("vulnerability_type").asText(null),
                    metadata.path("cwe").asText(null), "Main.java", line, integerValue(start.path("col")),
                    integerValue(end.path("line")), integerValue(end.path("col")), sourceLine(sourceCode, line)));
        }
        return List.copyOf(findings);
    }

    private Integer integerValue(JsonNode value) { return value.isInt() ? value.asInt() : null; }

    private String sourceLine(String sourceCode, Integer line) {
        if (line == null) return null;
        String[] lines = sourceCode.split("\\R", -1);
        return line > 0 && line <= lines.length ? lines[line - 1].strip() : null;
    }

    private String errorMessage(String stderr, String fallback) {
        return stderr == null || stderr.isBlank() ? fallback : stderr.strip();
    }

    private void deleteTemporaryDirectory(Path directory) {
        if (directory == null) return;
        try (var paths = Files.walk(directory)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                try { Files.deleteIfExists(path); } catch (IOException ignored) { }
            });
        } catch (IOException ignored) { }
    }
}

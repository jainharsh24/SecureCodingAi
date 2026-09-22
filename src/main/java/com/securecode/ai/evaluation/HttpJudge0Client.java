package com.securecode.ai.evaluation;

import com.securecode.ai.config.Judge0Properties;
import java.util.Locale;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpJudge0Client implements Judge0Client {
    private final RestClient restClient;
    private final Judge0Properties properties;

    public HttpJudge0Client(Judge0Properties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.baseUrl()).build();
        this.properties = properties;
    }

    @Override
    public Judge0ExecutionResult execute(String language, String sourceCode, String standardInput) {
        try {
            SubmissionToken submission = requestExecution(languageId(language), sourceCode, standardInput);
            if (submission == null || submission.token() == null || submission.token().isBlank()) {
                throw new Judge0UnavailableException("Judge0 did not return a submission token");
            }
            return waitForResult(submission.token());
        } catch (RestClientException exception) {
            throw new Judge0UnavailableException("Judge0 could not be reached", exception);
        }
    }

    private SubmissionToken requestExecution(int languageId, String sourceCode, String standardInput) {
        return postRequest().uri("/submissions?base64_encoded=false&wait=false")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new Judge0SubmissionRequest(sourceCode, languageId, standardInput))
                .retrieve().body(SubmissionToken.class);
    }

    private Judge0ExecutionResult waitForResult(String token) {
        long deadline = System.nanoTime() + properties.maxWaitMs() * 1_000_000;
        while (System.nanoTime() < deadline) {
            Judge0Submission submission = getRequest()
                    .uri("/submissions/{token}?base64_encoded=false&fields=status,stdout,stderr,compile_output,message", token)
                    .retrieve().body(Judge0Submission.class);
            if (submission != null && submission.status() != null && submission.status().id() > 2) {
                String details = firstPresent(submission.compile_output(), submission.stderr(), submission.message(),
                        submission.status().description());
                return new Judge0ExecutionResult(true, submission.status().id() == 3, submission.stdout(), details);
            }
            sleepBeforePollingAgain();
        }
        return new Judge0ExecutionResult(false, false, null, "Judge0 evaluation timed out");
    }

    private RestClient.RequestHeadersUriSpec<?> getRequest() {
        RestClient.RequestHeadersUriSpec<?> request = restClient.get();
        if (properties.authToken() != null && !properties.authToken().isBlank()) {
            request.header("X-Auth-Token", properties.authToken());
        }
        return request;
    }

    private RestClient.RequestBodyUriSpec postRequest() {
        RestClient.RequestBodyUriSpec request = restClient.post();
        if (properties.authToken() != null && !properties.authToken().isBlank()) {
            request.header("X-Auth-Token", properties.authToken());
        }
        return request;
    }

    private int languageId(String language) {
        return switch (language.trim().toLowerCase(Locale.ROOT)) {
            case "java" -> 62;
            case "python", "python3" -> 71;
            default -> throw new IllegalArgumentException("Judge0 supports only Java and Python challenges");
        };
    }

    private void sleepBeforePollingAgain() {
        try { Thread.sleep(properties.pollIntervalMs()); }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new Judge0UnavailableException("Judge0 evaluation was interrupted", exception);
        }
    }

    private String firstPresent(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return value;
        return "Execution was not accepted";
    }

    private record Judge0SubmissionRequest(String source_code, int language_id, String stdin) { }
    private record SubmissionToken(String token) { }
    private record Judge0Status(int id, String description) { }
    private record Judge0Submission(Judge0Status status, String stdout, String stderr, String compile_output, String message) { }
}

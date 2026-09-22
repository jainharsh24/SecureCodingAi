# SecureCode AI

Backend for a secure-coding learning platform that will evaluate functional correctness and security correctness separately.

## Current milestone

Phase 1.2: Spring Boot project foundation and PostgreSQL schema migration.

## Local PostgreSQL

Create the local database `securecode`, then copy `.env.example` to `.env` (or configure `.env.txt`) with the credentials for your local PostgreSQL installation. The schema is versioned in `src/main/resources/db/migration` and is applied by Flyway when the application starts.

## Authentication

Authentication uses BCrypt password hashes and a server-side HTTP session cookie backed by PostgreSQL user authentication.

| Endpoint | Access | Purpose |
| --- | --- | --- |
| `POST /api/auth/register` | Public | Creates a student account |
| `POST /api/auth/login` | Public | Creates an authenticated HTTP session |
| `/api/student/**` | `STUDENT` or `ADMIN` | Reserved student API area |
| `/api/admin/**` | `ADMIN` | Reserved admin API area |

Registration always assigns the `STUDENT` role. The `ADMIN` role is persisted and enforced by the security configuration, but cannot be self-assigned through a public API.

## Challenge management

Challenges are added only through the protected admin API; this project intentionally ships with no seeded challenge data.

| Endpoint | Required role |
| --- | --- |
| `GET /challenges` | `STUDENT` or `ADMIN` |
| `GET /challenges/{id}` | `STUDENT` or `ADMIN` |
| `POST /challenges` | `ADMIN` |
| `PUT /challenges/{id}` | `ADMIN` |
| `DELETE /challenges/{id}` | `ADMIN` |

Student responses include all challenge metadata and only test cases marked `hidden: false`. Admin creation and update responses include all test cases, so hidden functional-evaluation expected outputs are not exposed to students.

## Functional evaluation

`POST /challenges/{challengeId}/submissions` is available to authenticated students and admins. Its JSON body is `{ "sourceCode": "..." }`. The server sends each test case to Judge0 and returns `PASS`, `FAIL`, or `EVALUATION_ERROR` with the passed and total test counts. It does not compile or run submitted code locally.

Set `JUDGE0_BASE_URL` to an isolated Judge0 instance. The default is `http://localhost:2358`. Set `JUDGE0_AUTH_TOKEN` if that instance requires Judge0's `X-Auth-Token` header. Java challenges use Judge0 language ID 62 and Python uses ID 71.

## Semgrep security evaluation

For a Java submission that passes every functional test, the backend runs Semgrep as the first SAST evaluator and stores its raw JSON evidence in `security_evaluations`. Functional `FAIL` and `EVALUATION_ERROR` responses skip Semgrep completely. Semgrep analyzes a temporary source file only; it never executes student code.

The local rules are versioned in `semgrep-rules/java-security.yml`. On this Windows-hosted backend, Semgrep is started through WSL, so the defaults require a distribution named `Ubuntu` with Semgrep available in its login-shell PATH.

| Variable | Default | Purpose |
| --- | --- | --- |
| `SEMGREP_ENABLED` | `true` | Set to `false` to skip Semgrep. |
| `SEMGREP_WSL_DISTRIBUTION` | `Ubuntu` | WSL distribution containing Semgrep. |
| `SEMGREP_EXECUTABLE` | `semgrep` | Semgrep command in that distribution. |
| `SEMGREP_RULES_PATH` | `semgrep-rules/java-security.yml` | Local Semgrep rules file. |
| `SEMGREP_TIMEOUT_MS` | `30000` | Maximum time for one scan. |

## Run locally

With PostgreSQL running and Maven installed, run:

```powershell
mvn spring-boot:run
```

The application listens on `http://localhost:8080` by default.

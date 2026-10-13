# SecureCode AI

SecureCode AI is a Spring Boot secure-coding practice platform. Students authenticate, complete Java challenges, submit code for functional evaluation, and review persisted security evidence. Administrators manage users and challenges. The frontend is served as static HTML, CSS, and JavaScript.

The platform follows this basic flow:

```text
Student writes code
        ↓
Judge0 functional tests
        ↓
Semgrep and SpotBugs security evaluation
        ↓
Persisted analysis, history, and analytics
```

## Architecture

```text
src/main/java/com/securecode/ai/
├── controller/     HTTP API endpoints
├── dto/            request and response records
├── entity/         JPA entities and domain enums
├── repository/     Spring Data repositories
├── security/       bearer-token authentication and authorization
├── service/        challenge, submission, analysis, admin, and student read models
└── evaluation/
    ├── common/     persisted finding-evidence codec
    ├── judge0/     Judge0 client, result model, and configuration
    ├── semgrep/    WSL Semgrep client, result model, and normalization
    └── spotbugs/   SpotBugs/FindSecBugs client, result model, and configuration

src/main/resources/
├── db/migration/   immutable Flyway migrations
└── static/         student and administrator frontend

tools/semgrep/rules/java-security.yml  project Semgrep rules
```

### Current Evaluation Flow

1. Student selects a challenge.
2. The student writes or modifies the provided starter code.
3. The code is submitted to the backend.
4. Judge0 checks functional correctness.
5. If all functional tests pass, Semgrep and SpotBugs scan the code.
6. Evaluator evidence is stored with the submission and shown on the Analysis page, history, and analytics.

```text
PASS → Semgrep + SpotBugs → Persisted security evidence
FAIL → Stop
ERROR → Stop
```

## Technology Stack

* **Backend:** Java, Spring Boot
* **Database:** PostgreSQL
* **Database Migration:** Flyway
* **Authentication:** Spring Security + bearer tokens
* **Code Execution:** Judge0
* **Security Analysis:** Semgrep and SpotBugs/FindSecBugs
* **Frontend:** HTML, CSS, JavaScript
* **Build Tool:** Maven

## Configuration and prerequisites

Required tools are Java 25, Maven, PostgreSQL, a reachable Judge0 service, WSL with Semgrep installed in its configured distribution, and SpotBugs plus FindSecBugs and `javac.exe` for Windows SpotBugs scans.

Copy `.env.example` to `.env` and set local values. `.env` is ignored by Git; do not commit passwords, tokens, or production signing secrets. The template documents database, authentication, Judge0, Semgrep, and SpotBugs variables. `SEMGREP_RULES_PATH` defaults to `tools/semgrep/rules/java-security.yml`.

Semgrep runs inside `SEMGREP_WSL_DISTRIBUTION`; a Windows-only `semgrep.exe` is not sufficient for the current adapter. SpotBugs variables must reference the local `spotbugs.bat`, FindSecBugs plugin JAR, and JDK compiler.

## PostgreSQL and Flyway

The existing PostgreSQL database is preserved. Hibernate uses `ddl-auto: validate`, so it does not create or reset tables. Flyway validates and applies the immutable migrations in `src/main/resources/db/migration`; never alter, delete, renumber, or reapply an existing migration.

`flyway_schema_history` is Flyway's audit table. It retains the baseline plus applied migration versions and checksums and must remain with the existing data.

| Migration | Purpose |
| --- | --- |
| `V1__create_initial_schema.sql` | Creates the initial users, challenges, test cases, submissions, evaluations, feedback tables, and indexes. |
| `V2__add_challenge_details.sql` | Adds challenge vulnerability metadata, starter code, and ordered test cases. |
| `V3__add_functional_evaluation_status.sql` | Adds functional-evaluation detail text. |
| `V4__add_security_evaluation_status.sql` | Adds security-evaluator status and error fields. |
| `V5__add_learning_progress.sql` | Adds names, difficulty, attempts, learning score, assessment, constraints, and indexes. |
| `V6__add_challenge_numbers_and_injection_challenges.sql` | Adds challenge numbering, secure-reference storage, and seeded injection challenges/test cases. |

Create the database only if it does not exist (`CREATE DATABASE securecode;`), then let Flyway validate/migrate it. Do not reset an existing database.

### Start the application

Configure `.env`, PostgreSQL, and the external evaluators, then run:

```powershell
mvn spring-boot:run
```

Open:

```text
http://localhost:8080
```

## Testing

```powershell
mvn test
node --check src/main/resources/static/js/app.js
node --check src/main/resources/static/js/admin.js
```

The standard suite covers application startup/database migration validation, services, Semgrep normalization, and SpotBugs report parsing. The real SpotBugs integration test is opt-in:

```powershell
$env:RUN_SPOTBUGS_INTEGRATION_TESTS='true'
mvn "-Dtest=ProcessSpotBugsClientTests" test
```

## Known limitations

PMD and LLM evaluators are not implemented; the analysis board reports them as unavailable. The feedback panel is a UI placeholder. Judge0 must be started separately, and live Semgrep verification requires Semgrep inside the configured WSL distribution.

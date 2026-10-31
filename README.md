# SecureCode AI

**A Hands-On Secure Coding Practice and Multi-Engine Evaluation Platform**

SecureCode AI is a web-based learning platform designed to help students and developers learn how to write secure code through practical programming challenges.

Unlike traditional coding platforms that mainly evaluate whether code produces the correct output, SecureCode AI evaluates both **functional correctness and security correctness**.

Students solve vulnerability-based coding challenges, receive security feedback, understand their mistakes, and track their learning progress.

The platform currently focuses on **Injection Attacks** and provides an interactive environment for learning, practising, and improving secure coding skills.

## Key Features

- **Interactive Code Editor:** Write and submit code directly through the browser.
- **Vulnerability-Based Challenges:** 45 active challenges covering different injection vulnerabilities and difficulty levels.
- **Dual-Stage Evaluation:** Checks functional correctness first, followed by security analysis.
- **Multi-Engine Security Analysis:** Integrates Semgrep and SpotBugs/FindSecBugs, with Groq providing additional AI-based security advice.
- **Security Evidence Normalization:** Combines and normalizes security findings from different evaluators.
- **Four-Pillar Learning Score:** Measures functional correctness, security correctness, conceptual understanding, and independent problem-solving.
- **Learning Hub:** Provides educational material about vulnerabilities and secure coding practices.
- **Hints and Feedback:** Offers predefined hints and submission-specific learning feedback.
- **Submission History:** Allows students to review previous attempts and evaluation results.
- **Learning Analytics:** Tracks learning scores, challenge progress, and performance.
- **Prerequisite-Based Learning:** Organizes challenges into structured learning paths.
- **Admin Dashboard:** Supports user management and challenge creation, editing, and deletion.

## Challenge Catalog

The platform currently contains 45 active challenges across four difficulty levels.

| Difficulty | Number of Challenges |
|---|---:|
| Basic | 16 |
| Easy | 9 |
| Medium | 13 |
| Hard | 7 |
| **Total** | **45** |

The initial learning curriculum focuses on injection vulnerabilities, including:

- SQL Injection
- NoSQL Injection
- Command Injection
- LDAP Injection
- XPath Injection
- XML / XXE Injection
- CRLF / Email Header Injection
- Template / Code Injection

Challenges are organized with prerequisites to support progressive learning.

## System Architecture

The project follows a modular Spring Boot architecture.

```text
src/main/java/com/securecode/ai/
│
├── controller/       HTTP API endpoints
├── dto/              Request and response objects
├── entity/            JPA entities and domain enums
├── repository/        Spring Data repositories
├── security/          Authentication and authorization
├── service/            Business logic and application services
│
└── evaluation/
    ├── common/        Security evidence normalization
    ├── judge0/        Functional evaluation
    ├── semgrep/       Source-code security analysis
    └── spotbugs/      Bytecode security analysis

src/main/resources/
│
├── db/migration/      Flyway database migrations
├── templates/         Thymeleaf frontend templates
└── static/            CSS, JavaScript, and frontend assets

tools/
└── semgrep/rules/
    └── java-security.yml
```

## Evaluation Workflow

SecureCode AI uses a dual-stage evaluation process.

### Stage 1: Functional Evaluation

When a student submits code, Judge0 compiles and executes it against the challenge's test cases.

- Checks compilation and execution.
- Evaluates visible and hidden test cases.
- Determines whether the submission passes functional requirements.

### Stage 2: Security Evaluation

Security evaluation starts only after functional tests pass.

The security evaluators run concurrently:

| Evaluator | Purpose |
|---|---|
| Semgrep | Detects insecure coding patterns using source-code analysis and custom security rules. |
| SpotBugs / FindSecBugs | Detects security issues through Java bytecode analysis. |
| Groq AI | Provides additional contextual security analysis and advisory feedback. |

Groq is an advisory evaluator. Its output does not determine the authoritative security verdict or contribute to the learning score.

### Evaluation Flow

```text
Student Submits Code
         |
         v
   Judge0 Evaluation
         |
         v
   Functional Tests
         |
     +---+---+
     |       |
    FAIL    PASS
     |       |
     v       v
   Stop   Security Evaluators
              |
       +------+------+ 
       |      |      |
       v      v      v
    Semgrep SpotBugs Groq
       |      |      |
       +------+------+
              |
              v
      Evidence Normalization
              |
              v
      Security Assessment
              |
              v
      Learning Score and
      Feedback Generation
              |
              v
       Analysis and History
```

If functional evaluation fails, security evaluation is not performed for that submission.

## Learning Score System

SecureCode AI uses a four-pillar learning score with a maximum of 100 points.

| Component | Maximum Points |
|---|---:|
| Functional Correctness | 25 |
| Security Correctness | 45 |
| Conceptual Understanding | 15 |
| Independent Problem Solving | 15 |
| **Total** | **100** |

**1. Functional Correctness (25 points)**

Measures whether the submitted code passes the required functional test cases.

**2. Security Correctness (45 points)**

Measures whether the submitted code follows secure coding practices based on available security evaluation evidence.

**3. Conceptual Understanding (15 points)**

Measures the student's understanding of the vulnerability and the security fix through conceptual assessments.

**4. Independent Problem Solving (15 points)**

Measures how independently the student solves challenges, considering recorded assistance and hint usage.

The platform also distinguishes between functional completion, security verification, and mastery rather than treating every submission as simply passed or failed.

## Technology Stack

| Component | Technology |
|---|---|
| Backend | Java, Spring Boot |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Database Migration | Flyway |
| Authentication | Spring Security |
| Code Execution | Judge0 |
| Static Security Analysis | Semgrep |
| Bytecode Security Analysis | SpotBugs / FindSecBugs |
| AI Advisory | Groq API |
| Frontend | Thymeleaf, HTML, CSS, JavaScript |
| Build Tool | Maven |

## Configuration and Prerequisites

The following tools and services are required to run the project:

- Java 25
- Maven
- PostgreSQL
- Judge0 service
- WSL with Semgrep installed
- SpotBugs and FindSecBugs
- Windows JDK compiler (`javac.exe`) for the current SpotBugs integration

### Environment Configuration

1. Copy `.env.example` to `.env`.
2. Configure the PostgreSQL connection.
3. Configure authentication and token-related secrets.
4. Configure the Judge0 service URL.
5. Configure Semgrep WSL settings and rules path.
6. Configure SpotBugs, FindSecBugs, and JDK paths.
7. Configure Groq credentials if AI advisory evaluation is enabled.

The `.env` file is ignored by Git. Never commit passwords, API keys, tokens, or production secrets.

### Evaluator Configuration

**Semgrep**

Semgrep runs inside the configured WSL distribution. A Windows-only Semgrep installation is not sufficient for the current adapter.

The default custom rules path is:

```text
tools/semgrep/rules/java-security.yml
```

**SpotBugs / FindSecBugs**

Configure the local paths for:

- `spotbugs.bat`
- FindSecBugs plugin JAR
- Windows JDK compiler (`javac.exe`)

**Groq AI**

To enable the optional AI advisory evaluator:

```properties
GROQ_ENABLED=true
GROQ_API_KEY=your_groq_api_key
```

The evaluator uses the Groq Chat Completions API with structured output. The configured model is controlled by the application configuration.

Groq provides advisory findings and does not change the authoritative security assessment or learning score.

## Database and Flyway

SecureCode AI uses PostgreSQL for persistent storage.

Flyway manages database schema changes through versioned SQL migrations.

Important database practices:

- Hibernate uses `ddl-auto: validate`.
- Flyway validates and applies pending migrations.
- Existing database records are preserved.
- Applied migrations must not be modified, deleted, or renumbered.
- Never reset an existing database simply to resolve a migration issue.

The current project schema has progressed beyond the initial V6 migration. Always use the migration files available in the repository as the source of truth.

Create the database only if it does not already exist:

```sql
CREATE DATABASE securecode;
```

Then start the application and allow Flyway to validate and apply pending migrations.

## Running the Application

### 1. Clone the Repository

```bash
git clone <repository-url>
cd <repository-folder>
```

### 2. Configure Environment Variables

Copy `.env.example` to `.env` and configure the required services and credentials.

### 3. Start PostgreSQL

Ensure PostgreSQL is running and the configured database is accessible.

### 4. Start External Evaluators

Ensure Judge0, Semgrep (inside the configured WSL distribution), and SpotBugs/FindSecBugs are correctly configured.

### 5. Run the Spring Boot Application

```powershell
mvn spring-boot:run
```

### 6. Open the Application

```text
http://localhost:8080
```

## Testing

Run the project test suite:

```powershell
mvn test
```

Check frontend JavaScript syntax:

```powershell
node --check src/main/resources/static/js/app.js
node --check src/main/resources/static/js/admin.js
```

The tests cover application services, evaluation components, security evidence handling, and other application functionality.

The real SpotBugs integration test can be enabled separately:

```powershell
$env:RUN_SPOTBUGS_INTEGRATION_TESTS='true'
mvn "-Dtest=ProcessSpotBugsClientTests" test
```

External services such as Judge0 must be available for their corresponding live integration checks.

## Project Goals

SecureCode AI aims to:

1. Encourage students to write secure code rather than focusing only on correct output.
2. Provide practical experience with common software vulnerabilities.
3. Combine functional testing with automated security analysis.
4. Help learners understand security findings and fix vulnerabilities.
5. Measure learning progress through a structured scoring system.
6. Provide a foundation for expanding secure coding education to additional vulnerability categories.

## Future Scope

Potential future improvements include:

- Expanding the challenge catalog to cover the complete OWASP Top 10.
- Supporting additional programming languages.
- Adding more security analysis tools.
- Improving AI-assisted remediation and personalized feedback.
- Introducing advanced vulnerability categories.
- Expanding learning analytics and assessment capabilities.

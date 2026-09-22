# SecureCode AI

SecureCode AI is a secure coding practice platform where students solve programming challenges and learn to identify security vulnerabilities in their code.

The platform follows this basic flow:

```text
Student writes code
        ↓
Functional Evaluation (Judge0)
        ↓
Security Evaluation (Semgrep)
        ↓
Analysis & Feedback
```

## Current Stage

**Review 2 / Working Prototype**

The current version has the core submission pipeline working:

* Student registration and login
* Challenge management
* PostgreSQL database
* Java challenge execution using Judge0
* Functional test evaluation
* Semgrep security evaluation
* Security evidence normalization
* Basic frontend for students
* Code editor and submission
* Submission analysis page
* Session-based challenge history and analytics

### Current Evaluation Flow

1. Student selects a challenge.
2. The student writes or modifies the provided starter code.
3. The code is submitted to the backend.
4. Judge0 checks functional correctness.
5. If all functional tests pass, Semgrep checks the code for security vulnerabilities.
6. The result is shown on the Analysis page.

```text
PASS → Semgrep → Security Result
FAIL → Stop
ERROR → Stop
```

## Technology Stack

* **Backend:** Java, Spring Boot
* **Database:** PostgreSQL
* **Database Migration:** Flyway
* **Authentication:** Spring Security + HTTP Session
* **Code Execution:** Judge0
* **Security Analysis:** Semgrep
* **Frontend:** HTML, CSS, JavaScript
* **Build Tool:** Maven

## Project Structure

```text
securecode-ai/
├── src/main/java/          # Spring Boot backend
├── src/main/resources/
│   ├── db/migration/       # Flyway migrations
│   ├── semgrep-rules/      # Security rules
│   └── static/             # Frontend
├── pom.xml
└── README.md
```

## Running Locally

### Requirements

* Java 25
* Maven
* PostgreSQL
* Judge0
* WSL + Semgrep

### Start the application

Configure PostgreSQL and the required environment variables, then run:

```powershell
mvn spring-boot:run
```

Open:

```text
http://localhost:8080
```

## Future Work

The current prototype uses **Judge0 + Semgrep** as the working evaluation pipeline.

Future versions will add:

* SpotBugs
* PMD
* LLM-based security evaluation
* Evidence aggregation from multiple evaluators
* AI-generated feedback and hints
* Learning recommendations
* Persistent student analytics and submission history

The goal is to combine these components into a complete **AI-guided secure coding practice platform**.

from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from report_builder.styles import (
    add_chapter_heading, add_sec_heading, add_subsec_heading, 
    add_p, add_bullet, create_styled_table, NAVY_BLUE, DARK_GRAY
)

def generate_chapter2(doc):
    add_chapter_heading(doc, 2, "Problem Statement & Requirements Engineering")

    intro_p = (
        "This chapter formalizes the engineering problem addressed by SecureCode AI, details the architectural features "
        "of the proposed platform, enumerates the precise technical objectives, and specifies the functional and non-functional "
        "requirements that govern the system's design and empirical evaluation."
    )
    add_p(doc, intro_p, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    # 2.1 Problem Statement
    add_sec_heading(doc, "2.1", "Problem Statement")

    ps_p1 = (
        "Modern software development platforms and automated programming assessment systems suffer from a severe architectural "
        "and pedagogical limitation: they evaluate student code submissions purely on functional correctness (input-output matching), "
        "completely ignoring code security, architectural hygiene, and vulnerability resistance. Consequently, novice developers "
        "routinely write, test, and submit code containing severe security vulnerabilities—such as SQL Injection, Command Injection, "
        "and Path Traversal—and receive full credit as long as the functional test cases pass. This establishes negative reinforcement, "
        "teaching developers that insecure coding practices are acceptable as long as functional requirements are met."
    )
    add_p(doc, ps_p1, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    ps_p2 = (
        "Furthermore, existing cybersecurity learning environments are heavily skewed toward offensive security (penetration testing, "
        "exploit scripting, and red teaming) rather than defensive software engineering. Platforms such as PortSwigger Web Security "
        "Academy or Hack The Box train users to break into vulnerable systems using automated attack tools, but provide no integrated "
        "code editor, no compiler feedback, and no automated mechanism to evaluate whether a user can write secure, production-ready "
        "source code that satisfies business logic while resisting malicious payloads. Meanwhile, commercial enterprise secure coding "
        "tools are closed, prohibitively expensive, and rely on multiple-choice quizzes or micro-lessons rather than authentic, hands-on "
        "code refactoring."
    )
    add_p(doc, ps_p2, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    ps_p3 = (
        "The problem addressed in this project is to develop and empirically validate a unified, automated secure programming "
        "practice and assessment platform—SecureCode AI—that evaluates both functional correctness and security properties, integrates "
        "multi-engine static security analysis (Semgrep, SpotBugs, and Groq LLM), prevents superficial trial-and-error guessing through "
        "attempt-limiting windows and prerequisite learning graphs, verifies conceptual comprehension via interactive assessments, and "
        "calculates an auditable, four-pillar learning score."
    )
    add_p(doc, ps_p3, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    # 2.2 Features of the System
    add_sec_heading(doc, "2.2", "Features of the System")

    add_bullet(doc, "An in-browser code editor featuring Monaco Editor / React, syntax highlighting, keyboard shortcuts, starter code loading, and responsive split-pane layout showing problem descriptions, constraints, and test suites side-by-side.", bold_prefix="Interactive Browser-Based Code Editor: ")
    add_bullet(doc, "A curated curriculum of 45 staged injection challenges structured across 6 critical injection subtypes (SQL, NoSQL, OS Command, LDAP, XPath, Header/CRLF, Code Injection) and 4 difficulty tiers (Basic, Easy, Medium, Hard).", bold_prefix="Active Injection Challenge Catalog: ")
    add_bullet(doc, "A staged execution architecture where dynamic functional unit testing (Judge0 sandbox) is evaluated first. Only solutions that successfully compile and pass all functional test cases advance to the static security evaluation stage, preventing wasted computational overhead on broken submissions.", bold_prefix="Dual-Stage Evaluation Engine: ")
    add_bullet(doc, "Simultaneous scanning of student source code using Semgrep (AST pattern matching and taint tracking), SpotBugs with FindSecBugs (bytecode dataflow analysis), and Groq LLaMA-3 (contextual reasoning and architectural advice).", bold_prefix="Multi-Engine Security Analysis Pipeline: ")
    add_bullet(doc, "An automated arbitration module that parses disparate findings from all scanners, resolves discrepancies, filters false positives, and assigns a normalized security verdict (VERIFIED_SAFE, TARGETED_VULNERABILITY, UNVERIFIED) with an associated evidence confidence score.", bold_prefix="Evidence Normalization and Consensus: ")
    add_bullet(doc, "A rate-limiting mechanism that allocates exactly 5 graded submission attempts per challenge across a rolling 7-day window. This curtails rapid trial-and-error guessing and compels learners to think deliberately before submitting.", bold_prefix="Five-Attempt Rate-Limiting Window: ")
    add_bullet(doc, "A three-level assistance engine providing Conceptual Hints (-5 pts), Implementation Hints (-10 pts), and Solution Reveals (-15 pts) to balance guided learning with independent achievement.", bold_prefix="Tiered Hint Assistance Engine: ")
    add_bullet(doc, "Post-submission comprehension verification requiring learners to answer conceptual multiple-choice questions and submit written architectural justifications explaining how their fix prevents vulnerability exploitation.", bold_prefix="Conceptual Understanding Assessments: ")
    add_bullet(doc, "A mathematical scoring model combining Functional Correctness (25%), Security Correctness (45%), Conceptual Understanding (15%), and Independent Problem Solving (15%) into a verified 100-point score, enforcing a 70-point Solved threshold and 90-point Mastery threshold.", bold_prefix="Four-Pillar Overall Learning Score: ")
    add_bullet(doc, "A directed acyclic graph (DAG) enforcing prerequisite foundational challenges before advanced challenges can be unlocked, ensuring structured pedagogical progression.", bold_prefix="Prerequisite Progression Graph: ")
    add_bullet(doc, "Comprehensive visual dashboards displaying difficulty-wise completion rates, functional pass accuracy, longitudinal learning trends, and vulnerability remediation history.", bold_prefix="Learning Analytics and Performance Tracking: ")
    add_bullet(doc, "A dedicated educational resource section detailing injection definitions, real-world attack vectors, impact scenarios, and defensive programming principles for each vulnerability category.", bold_prefix="Learning Hub Educational Repository: ")

    # 2.3 Objectives
    add_sec_heading(doc, "2.3", "Objectives")

    obj_p = "The technical and pedagogical development of SecureCode AI is guided by the following concrete objectives:"
    add_p(doc, obj_p, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    add_bullet(doc, "Design and construct a scalable, microservices-ready client-server architecture utilizing Spring Boot 3, Java 25, PostgreSQL, and a modern responsive web client.", bold_prefix="1. Full-Stack Architectural Engineering: ")
    add_bullet(doc, "Implement a sandboxed execution environment integrating Judge0 API to compile and dynamically test Java source code against hidden and visible test cases within 2.0s execution boundaries.", bold_prefix="2. Sandboxed Functional Execution: ")
    add_bullet(doc, "Integrate, orchestrate, and normalize findings from Semgrep, SpotBugs, and Groq LLM into an auditable security consensus engine with zero false passes on vulnerable test cases.", bold_prefix="3. Multi-Engine Security Consensus: ")
    add_bullet(doc, "Populate and maintain an active catalog of 45 staged challenges across 6 injection subtypes with complete reference implementations and verified automated test suites.", bold_prefix="4. Challenge Curriculum Development: ")
    add_bullet(doc, "Deploy attempt-limiting windows (5 attempts / 7 days) and prerequisite unlock graphs to enforce deliberate problem-solving habits.", bold_prefix="5. Pedagogical Scaffolding & Rate Limiting: ")
    add_bullet(doc, "Formulate and validate the 4-pillar learning score formula, proving that genuine security mastery requires both code correctness and conceptual comprehension.", bold_prefix="6. Multi-Factor Competency Assessment: ")
    add_bullet(doc, "Execute an empirical evaluation across 80 automated unit, integration, and security test suites to prove system reliability, sub-second latency, and data consistency.", bold_prefix="7. Empirical Verification & Benchmarking: ")

    # 2.4 System Specifications & Requirements
    add_sec_heading(doc, "2.4", "System Specifications & Requirements")

    # 2.4.1 Functional Requirements
    add_subsec_heading(doc, "2.4.1", "Functional Requirements")

    fr_intro = "The functional requirements define the specific operational capabilities and services provided by SecureCode AI:"
    add_p(doc, fr_intro, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    add_bullet(doc, "The system shall provide secure user registration, authentication, role-based authorization (STUDENT, ADMIN), and session token management with PBKDF2 password hashing.", bold_prefix="FR-1 (Authentication & Authorization): ")
    add_bullet(doc, "The system shall display an active challenge catalog filtered by vulnerability subtype, difficulty tier, and completion status, enforcing prerequisite dependencies before unlocking advanced challenges.", bold_prefix="FR-2 (Challenge Management & Browsing): ")
    add_bullet(doc, "The system shall provide an in-browser code editor loaded with initial vulnerable starter code, challenge problem statements, input-output specifications, and visible test cases.", bold_prefix="FR-3 (Interactive Code Workspace): ")
    add_bullet(doc, "The system shall execute student code inside a sandboxed runtime against predefined test cases, returning detailed execution output, compiler errors, or test failure diffs within 2.0 seconds.", bold_prefix="FR-4 (Sandboxed Functional Evaluation): ")
    add_bullet(doc, "Upon functional pass, the system shall invoke Semgrep and SpotBugs static analyzers and Groq LLM, parse their outputs, and produce a normalized security verdict with identified CWE numbers and line numbers.", bold_prefix="FR-5 (Multi-Engine Security Evaluation): ")
    add_bullet(doc, "The system shall generate contextual remediation advice, detailing why the code is vulnerable, what secure API to use, and how to refactor the implementation.", bold_prefix="FR-6 (Actionable Remediation Feedback): ")
    add_bullet(doc, "The system shall administer post-submission comprehension questions and evaluate written justifications using an automated rubric.", bold_prefix="FR-7 (Conceptual Assessment Engine): ")
    add_bullet(doc, "The system shall compute the 4-pillar learning score, record attempt positions (1 to 5), update user progress analytics, and update student performance dashboards.", bold_prefix="FR-8 (Scoring & Learning Analytics): ")

    # 2.4.2 Non-Functional Requirements
    add_subsec_heading(doc, "2.4.2", "Non-Functional Requirements")

    nfr_intro = (
        "The non-functional requirements define the quality attributes, security boundaries, performance constraints, "
        "and operational standards governing SecureCode AI. Table 2.4.1 summarizes the complete specification."
    )
    add_p(doc, nfr_intro, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    nfr_table_data = [
        ["NFR-SEC-1", "Sandbox Isolation", "All dynamic functional code execution must take place inside an isolated sandbox container with restricted network access, read-only filesystem mounts, and strict memory/CPU caps.", "Critical"],
        ["NFR-SEC-2", "Zero Insecure Passes", "The security evaluation engine must never assign a VERIFIED_SAFE verdict to any code submission containing an active, unmitigated targeted vulnerability.", "Critical"],
        ["NFR-SEC-3", "Data Protection & Privacy", "User passwords must be hashed using salted PBKDF2 with 200,000+ iterations. API access must require cryptographically signed Bearer authentication tokens.", "High"],
        ["NFR-SEC-4", "Auditability & Logging", "All code submissions, evaluator raw outputs, consensus decisions, hint views, and assessment scores must be immutably recorded in PostgreSQL audit tables.", "High"],
        ["NFR-AI-1", "Evidence Traceability", "100% of security findings and remediation suggestions produced by the AI advisory layer must cite specific code lines and mapped CWE identifiers.", "High"],
        ["NFR-AI-2", "Hallucination Filtering", "The consensus normalizer must filter out any AI-generated security claim that contradicts deterministic static analysis findings (Semgrep/SpotBugs).", "High"],
        ["NFR-AI-3", "Safe Fallback on API Outage", "If the Groq LLM API is unavailable, the platform must degrade gracefully to static analysis mode without blocking evaluation or losing student code.", "High"],
        ["NFR-AI-4", "Advisory Grounding", "AI explanations must adhere strictly to established secure coding standards (OWASP Top 10, CWE, CERT Oracle Secure Coding Standard for Java).", "Medium"],
        ["NFR-PERF-1", "Functional Latency", "Dynamic functional test execution via the sandboxed runner must complete and return results within 2,500 milliseconds for 95% of requests.", "High"],
        ["NFR-PERF-2", "Security Scan Latency", "Local static analysis (Semgrep AST matching) must complete within 1,200 milliseconds per submission.", "Medium"],
        ["NFR-PERF-3", "Total Evaluation Pipeline", "End-to-end evaluation (Functional + Static + AI + Scoring) must complete within 5,000 milliseconds under normal network conditions.", "Medium"],
        ["NFR-REL-1", "Transactional Integrity", "Submission scoring, attempt consumption, and prerequisite unlocks must execute within ACID transactions to prevent race conditions.", "Critical"],
        ["NFR-REL-2", "Schema Migration Consistency", "All relational database evolutions must be automated via Flyway migrations with strict versioning and checksum verification.", "High"],
        ["NFR-USE-1", "Responsive Interface", "The web application must render seamlessly across desktop (1920x1080) and laptop (1366x768) resolutions with touch-accessible controls.", "Medium"],
        ["NFR-USE-2", "Clear Feedback Presentation", "Evaluation results must present color-coded visual indicators (Green for Safe, Amber for Warning, Red for Vulnerable) with expandable diffs.", "Medium"]
    ]

    create_styled_table(
        doc,
        ["Requirement ID", "Category / Title", "Specification & Acceptance Criteria", "Priority"],
        nfr_table_data,
        col_widths=[1.3, 1.4, 2.9, 0.8],
        table_title="Comprehensive Non-Functional Requirements Specification",
        table_id="Table 2.4.1"
    )

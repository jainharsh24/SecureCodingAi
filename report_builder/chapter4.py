from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from report_builder.styles import (
    add_chapter_heading, add_sec_heading, add_subsec_heading, add_subsubsec_heading,
    add_p, add_bullet, add_figure, create_styled_table, NAVY_BLUE, DARK_GRAY
)

def generate_chapter4(doc):
    add_chapter_heading(doc, 4, "Design and Implementation")

    intro_p = (
        "This chapter presents the architectural design, formal modeling, algorithmic formulations, relational data "
        "structures, and implementation mechanics of SecureCode AI. The platform is engineered as a robust, decoupled, "
        "multi-tier system built on Spring Boot 3, Java 25, PostgreSQL, and a responsive web application client, integrating "
        "sandboxed dynamic execution with a multi-engine static security analysis and AI advisory pipeline."
    )
    add_p(doc, intro_p, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    # 4.1 System Architecture
    add_sec_heading(doc, "4.1", "System Architecture")

    arch_p1 = (
        "SecureCode AI follows a modern, highly modular, client-server architecture organized into four primary functional tiers, "
        "as illustrated in Fig 4.1.1:"
    )
    add_p(doc, arch_p1, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    add_bullet(doc, "The client application provides an intuitive, responsive user interface accessible via modern web browsers on desktop, laptop, and tablet devices. Engineered with HTML5, CSS3, Bootstrap, and vanilla/React JavaScript components, it features an embedded Monaco code editor, real-time submission consoles, dynamic learning analytics dashboards, interactive hint dialogs, and administrative management panels.", bold_prefix="1. Presentation Tier (Client Web Application): ")
    add_bullet(doc, "The backend server is implemented in Spring Boot 3 (Java 25) following a domain-driven, layered service architecture. It encapsulates six core business service modules: UserService (authentication, profile, and JWT token management), ChallengeService (catalog retrieval, difficulty filtering, and prerequisite DAG evaluation), SubmissionService (submission intake, attempt window enforcement, and persistence), FeedbackService (remediation rendering, fix advice, and CWE linking), ProgressService (learning score computation and analytics aggregation), and AdminService (challenge CRUD and user management).", bold_prefix="2. Application & Logic Tier (Spring Boot Backend): ")
    add_bullet(doc, "The core computational backbone of SecureCode AI. To optimize throughput and guarantee security isolation, the evaluation engine is split into two specialized sub-stages: (a) Functional Evaluation Runner (integrating the Judge0 API sandbox for isolated compilation and dynamic execution against unit test suites), and (b) Security & AI Evaluation Pipeline (orchestrating Semgrep AST analysis, SpotBugs/FindSecBugs bytecode analysis, and Groq LLaMA-3 contextual LLM advisory).", bold_prefix="3. Dual-Stage Evaluation Engine Tier: ")
    add_bullet(doc, "Enterprise-grade relational persistence powered by PostgreSQL, managed through version-controlled Flyway schema migrations (V1 to V21). The database stores users, challenge definitions, test suites, immutable submission records, raw evaluator outputs, normalized security findings, hint view logs, and conceptual understanding assessments.", bold_prefix="4. Persistence & Data Tier (PostgreSQL): ")

    add_figure(doc, "images_&_ppt/Architecture.png", "Fig 4.1.1", "SecureCode AI Proposed Multi-Tier System Architecture", width_inches=6.2)

    # 4.2 Working of the System
    add_sec_heading(doc, "4.2", "Working of the System")

    work_p1 = (
        "The end-to-end operational workflow of SecureCode AI is designed to guide learners seamlessly through a structured "
        "defensive programming cycle while enforcing rigorous academic and security checks:"
    )
    add_p(doc, work_p1, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    add_bullet(doc, "A student registers or authenticates using credentials. The authentication service validates credentials using PBKDF2 hashing and issues a Bearer JWT token valid for the active session.", bold_prefix="Step 1 (Authentication & Session Initialization): ")
    add_bullet(doc, "The student browses the Challenge Catalog. The PrerequisiteAccessService evaluates the student's completed submission graph against prerequisite edges. Locked challenges display clear prerequisite requirements, while unlocked challenges display difficulty badges, CWE tags, and remaining attempt counters.", bold_prefix="Step 2 (Challenge Selection & Prerequisite Verification): ")
    add_bullet(doc, "Upon selecting a challenge, the workspace loads the problem description, vulnerable starter code, functional constraints, visible test cases, and real-world vulnerability impact context. The student reviews the insecure code in the Monaco Editor and formulates a defensive refactoring strategy.", bold_prefix="Step 3 (Vulnerability Exploration & Code Refactoring): ")
    add_bullet(doc, "If the student encounters conceptual roadblocks, they may open the Tiered Hint modal. Viewing a Conceptual Hint deducts 5 points from the independence score; an Implementation Hint deducts 10 points; a Full Solution Reveal deducts 15 points. All views are logged immutably in hint_view_events.", bold_prefix="Step 4 (Tiered Hint Assistance): ")
    add_bullet(doc, "When the student clicks 'Submit Code', the AttemptWindowService verifies that the student has remaining attempts (within the 5-attempt / 7-day allowance) and atomicity increments the attempt counter.", bold_prefix="Step 5 (Submission Intake & Attempt Rate Limiting): ")
    add_bullet(doc, "The SubmissionService transmits the source code to the Judge0 sandbox. Judge0 compiles and executes the code against hidden and visible test cases. If any test case fails, compilation errors occur, or runtime limits (2.0s CPU / 128MB) are exceeded, evaluation halts immediately, returning detailed execution diffs to the student.", bold_prefix="Step 6 (Stage 1 - Sandboxed Functional Evaluation): ")
    add_bullet(doc, "If functional execution achieves a 100% PASS verdict, the system automatically triggers Stage 2. The source code is scanned in parallel by Semgrep (AST pattern matching), SpotBugs/FindSecBugs (bytecode dataflow analysis), and Groq LLaMA-3 (contextual semantic advisory).", bold_prefix="Step 7 (Stage 2 - Multi-Engine Security Analysis): ")
    add_bullet(doc, "The SecurityFindingEvidenceCodec and SecurityCorrectnessScoringService normalize disparate outputs into a unified verdict: VERIFIED_SAFE (no vulnerabilities detected), TARGETED_VULNERABILITY (the challenge's targeted flaw remains unmitigated), or UNVERIFIED (evaluator divergence).", bold_prefix="Step 8 (Evidence Normalization & Consensus Arbitration): ")
    add_bullet(doc, "Upon achieving a functionally correct and safe submission, the student is prompted to complete a post-submission Conceptual Understanding Quiz. The student answers multiple-choice questions regarding attack vectors and inputs a written architectural justification explaining why their defensive implementation breaks the exploit path.", bold_prefix="Step 9 (Conceptual Understanding Assessment): ")
    add_bullet(doc, "The OverallLearningScoreService computes the final 100-point learning score combining Functional Correctness (25), Security Correctness (45), Understanding (15), and Independence (15). Results are displayed on the Submission Analysis page, and progress metrics update across the student dashboard.", bold_prefix="Step 10 (Multi-Pillar Scoring & Dashboard Analytics): ")

    # 4.3 Use Case Modeling
    add_sec_heading(doc, "4.3", "Use Case Modeling")

    uc_p = (
        "The functional interactions between external actors and SecureCode AI are formalized in Fig 4.3.1. "
        "The system defines three primary actors: (1) Student / Developer (the core learner executing challenges and reviewing feedback), "
        "(2) System Administrator (managing user accounts, authoring challenges, and monitoring system-wide analytics), and "
        "(3) Evaluation Engine (an autonomous internal actor orchestrating Judge0, Semgrep, SpotBugs, and Groq LLM)."
    )
    add_p(doc, uc_p, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    add_bullet(doc, "Includes Register/Login, Browse Challenges, View Challenge Details, Submit Solution, View Analysis & Feedback, and View Submission History & Progress.", bold_prefix="Student Use Cases: ")
    add_bullet(doc, "Includes Manage Users, Manage Challenges (CRUD operations, starter code authoring, test suite creation), View System Analytics & Submissions, and View Reports & Audit Logs.", bold_prefix="Admin Use Cases: ")
    add_bullet(doc, "The 'Submit Solution' use case incorporates an <<include>> relationship to 'Execute Functional Tests (Judge0)' and an <<include>> relationship to 'Generate AI Feedback & Remediation'. Crucially, it incorporates an <<extend>> relationship to 'Run Security Analysis (Semgrep & SpotBugs)' with an explicit extension point: 'If functional tests pass'.", bold_prefix="Execution Engine Relationships: ")

    add_figure(doc, "images_&_ppt/UseCase_diagram.png", "Fig 4.3.1", "Comprehensive Use Case Diagram for Students, Administrators, and Evaluation Engine", width_inches=6.2)

    # 4.4 Data Flow Diagrams
    add_sec_heading(doc, "4.4", "Data Flow Diagrams (Level 0, 1, and 2)")

    dfd_p = (
        "Data flow diagrams model how information propagates through SecureCode AI across hierarchical levels of abstraction. "
        "Fig 4.4.1 illustrates the Level 0 Context Diagram; Fig 4.4.2 depicts the Level 1 Subsystem Flow; and Fig 4.4.3 presents "
        "the detailed Level 2 Explosion of the Evaluation Engine."
    )
    add_p(doc, dfd_p, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    # DFD Level 0
    add_subsec_heading(doc, "4.4.1", "Data Flow Diagram – Level 0 (Context Diagram)")
    add_p(doc, "The Context Diagram (Fig 4.4.1) models the entire SecureCode AI system as a single central process (0.0) interfacing with two external entities: Student / Developer and System Administrator. Data flows into the system include registration credentials, challenge requests, and code submissions from students, along with user management requests and challenge test suite data from administrators. Output flows include authentication session results, challenge details, submission verdicts, AI remediation advice, dashboard progress data, and administrative status reports.")
    add_figure(doc, "images_&_ppt/DFD_0_Diagram.drawio.png", "Fig 4.4.1", "Data Flow Diagram – Level 0 (Context Diagram)", width_inches=6.0)

    # DFD Level 1
    add_subsec_heading(doc, "4.4.2", "Data Flow Diagram – Level 1 (System Processes)")
    add_p(doc, "The Level 1 Diagram (Fig 4.4.2) decomposes the central process into seven distinct sub-processes and three primary data stores: Process 1.0 (Login / Register), Process 2.0 (Request Challenges), Process 3.0 (Submit Code), Process 4.0 (Evaluate Code), Process 5.0 (Generate Feedback), Process 6.0 (Manage Users), and Process 7.0 (Manage Challenges). The processes interact with Data Store D1 (User DB), Data Store D2 (Challenge DB), and Data Store D3 (Results & Submissions DB).")
    add_figure(doc, "images_&_ppt/DFD_1_Diagram.drawio.png", "Fig 4.4.2", "Data Flow Diagram – Level 1 (Core Subsystems and Data Stores)", width_inches=6.2)

    # DFD Level 2
    add_subsec_heading(doc, "4.4.3", "Data Flow Diagram – Level 2 (Evaluation Engine Decomposition)")
    add_p(doc, "The Level 2 Diagram (Fig 4.4.3) explodes Process 4.0 (Evaluate Code) into its constituent analytical pipeline. Process 4.1 (Functional Evaluation) queries test cases from D2: Challenge DB and coordinates with the external Judge0 API. If functional tests fail, the functional result bypasses static analysis and triggers Process 5.0 (Generate Feedback). If functional tests pass, Process 4.2 (Static Security Analysis) dispatches simultaneous scan requests to Semgrep and SpotBugs, while routing code to Process 4.3 (AI Vulnerability Analysis / Groq LLM). All raw findings feed into Process 4.4 (Result Normalization), which computes normalized evidence vectors for Process 4.5 (Overall Evaluation). Finally, Process 4.5 stores verified outcomes in D3: Results & Submissions DB and sends final verdicts to Process 5.0.")
    add_figure(doc, "images_&_ppt/DFD_2_Diagram.drawio.png", "Fig 4.4.3", "Data Flow Diagram – Level 2 (Evaluation Engine Decomposition & Normalization Pipeline)", width_inches=4.2)

    # 4.5 Evaluation Workflow & Activity Modeling
    add_sec_heading(doc, "4.5", "Evaluation Workflow & Activity Modeling")

    act_p = (
        "The runtime coordination and decision boundaries of the submission pipeline are formalized in the Activity Diagram (Fig 4.5.1). "
        "The diagram employs four horizontal swimlanes: User, System (Application), Evaluation Engine, and Database (PostgreSQL). "
        "Key decision forks illustrate the dual-stage evaluation gate: functional execution failure immediately returns an error result "
        "and updates submission logs, terminating the pipeline; conversely, a functional pass triggers parallel static security scans, "
        "Groq LLM evaluation, and holistic learning score aggregation."
    )
    add_p(doc, act_p, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    add_figure(doc, "images_&_ppt/Activity_diagram.png", "Fig 4.5.1", "Activity Diagram & Submission Evaluation Workflow", width_inches=6.2)

    # 4.6 Multi-Engine Security Architecture & Consensus
    add_sec_heading(doc, "4.6", "Multi-Engine Security Architecture & Consensus")

    sec_p1 = (
        "A foundational contribution of SecureCode AI is its multi-evaluator consensus mechanism, engineered to eliminate "
        "the 20%+ false-negative rate inherent in single-scanner SAST platforms [1], [7]. SecureCode AI deploys three complementary "
        "security evaluators:"
    )
    add_p(doc, sec_p1, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    add_bullet(doc, "Operates directly on the Java Abstract Syntax Tree (AST). Semgrep utilizes curated YAML rule definitions located in tools/semgrep/rules/ targeted to specific injection patterns (e.g., detecting java.sql.Statement.executeQuery with string concatenation, Runtime.getRuntime().exec with unsanitized arguments, or LDAP search filters with raw input). Semgrep delivers deterministic, millisecond-fast pattern recognition.", bold_prefix="1. Semgrep SAST Engine: ")
    add_bullet(doc, "Performs interprocedural bytecode dataflow and taint-tracking analysis on compiled .class artifacts. Utilizing the FindSecBugs security plugin, SpotBugs inspects data propagation from untrusted input sources to sensitive sinks, identifying taint vulnerabilities that span multiple methods or classes.", bold_prefix="2. SpotBugs / FindSecBugs Engine: ")
    add_bullet(doc, "Powered by Groq's high-speed inference engine running LLaMA-3. Unlike static linters that search for static syntactic signatures, the LLM analyzes the overall architectural intent of the code, identifying subtle logic flaws, improper allow-list implementations, and insufficient sanitization routines while generating natural-language remediation guidance.", bold_prefix="3. Groq LLM Contextual Advisor: ")

    con_p = (
        "To reconcile conflicting outputs, the NormalizedEvaluatorEvidence and SecurityEvidenceSummary services implement "
        "a strict fail-closed consensus rule: if ANY valid static scanner detects an active targeted vulnerability matching the "
        "challenge's CWE subtype, the security verdict is immediately classified as TARGETED_VULNERABILITY, and Security Correctness "
        "receives 0 points. A VERIFIED_SAFE verdict requires 100% of available evaluators to affirm that no targeted vulnerabilities "
        "exist. This zero-insecure-pass guarantee ensures that students can never bypass security requirements through scanner discrepancies."
    )
    add_p(doc, con_p, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    # 4.7 Algorithms and Mathematical Formulations
    add_sec_heading(doc, "4.7", "Algorithms and Mathematical Formulations")

    add_subsec_heading(doc, "4.7.1", "Dual-Stage Evaluation Gate Algorithm")
    alg1_text = (
        "Let C be the target challenge, S the submitted source code, and T = {t1, t2, ..., tn} the set of functional unit tests. "
        "Stage 1 dynamic evaluation produces a functional verdict V_func in {PASS, FAIL, COMPILE_ERROR, TIMEOUT}. "
        "Stage 2 security analysis is invoked if and only if V_func = PASS:\n\n"
        "Gate(S, T) = \n"
        "  Reject(V_func),                           if V_func != PASS\n"
        "  TriggerSecurityPipeline(S, C),            if V_func == PASS"
    )
    add_p(doc, alg1_text, italic=True, align=WD_ALIGN_PARAGRAPH.CENTER)

    add_subsec_heading(doc, "4.7.2", "Static Analysis Evidence Normalization Algorithm")
    alg2_text = (
        "Let E = {e1, e2, ..., em} be the set of completed security evaluators (Semgrep, SpotBugs, LLM). For each evaluator e_i, "
        "let v_i in {SAFE, VULNERABLE, UNAVAILABLE} be its verdict, and let F_i be the set of detected findings. "
        "The targeted vulnerability set is computed as:\n\n"
        "F_targeted = Union_{i=1}^m { f in F_i | CWE(f) == CWE(C) and v_i == VULNERABLE }\n\n"
        "The normalized security verdict V_sec is derived as:\n"
        "  TARGETED_VULNERABILITY,   if |F_targeted| > 0\n"
        "  VERIFIED_SAFE,            if |F_targeted| == 0 and forall i (v_i == SAFE)\n"
        "  UNVERIFIED,               if valid evaluators == 0 or conflicting evidence"
    )
    add_p(doc, alg2_text, italic=True, align=WD_ALIGN_PARAGRAPH.CENTER)

    add_subsec_heading(doc, "4.7.3", "Four-Pillar Overall Learning Score Formulation")
    alg3_text = (
        "The Overall Learning Score S_overall in [0, 100] is calculated across four bounded components:\n\n"
        "S_overall = S_func + S_sec + S_und + S_ind\n\n"
        "where:\n"
        "  S_func in [0, 25]: Functional correctness score awarded upon passing unit tests (Judge0).\n"
        "  S_sec in [0, 45]: Security correctness score. S_sec = 45 if V_sec = VERIFIED_SAFE; S_sec = 0 if V_sec = TARGETED_VULNERABILITY.\n"
        "  S_und in [0, 15]: Conceptual understanding score assessed via post-submission multiple-choice and written justifications.\n"
        "  S_ind in [0, 15]: Independent problem-solving score based on hint usage:\n"
        "      S_ind = 15 (No hints viewed)\n"
        "      S_ind = 10 (Conceptual hint viewed)\n"
        "      S_ind = 5  (Implementation hint viewed)\n"
        "      S_ind = 0  (Solution reveal viewed)\n\n"
        "Thresholds: A challenge is marked SOLVED when S_func + S_sec >= 70. A challenge achieves MASTERY when S_overall >= 90 "
        "and min(S_func, S_sec, S_und, S_ind) > 10."
    )
    add_p(doc, alg3_text, italic=True, align=WD_ALIGN_PARAGRAPH.CENTER)

    add_subsec_heading(doc, "4.7.4", "Attempt Windowing & Prerequisite Progression")
    alg4_text = (
        "Submission rate-limiting restricts attempts to exactly 5 per rolling 7-day window W per user u and challenge C:\n\n"
        "WindowCheck(u, C, now) = \n"
        "  ResetWindow(u, C, attempts=1),          if now - window_start > 7 days\n"
        "  IncrementAttempts(u, C),                if now - window_start <= 7 days and attempts < 5\n"
        "  RejectSubmission('Attempt Limit Met'),  if now - window_start <= 7 days and attempts >= 5\n\n"
        "Prerequisite unlock logic evaluates the challenge dependency graph DAG = (V, E_pre). Challenge C_target is UNLOCKED if:\n"
        "  forall (C_pre, C_target) in E_pre: isVerifiedCompleted(u, C_pre) == True"
    )
    add_p(doc, alg4_text, italic=True, align=WD_ALIGN_PARAGRAPH.CENTER)

    # 4.8 Relational Data Model & Database Schema
    add_sec_heading(doc, "4.8", "Relational Data Model & Database Schema")

    db_p = (
        "SecureCode AI utilizes a normalized PostgreSQL relational database schema managed through 21 versioned Flyway migrations. "
        "Table 4.8.1 summarizes the 13 core relational tables, their functional roles, and key constraints."
    )
    add_p(doc, db_p, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    db_table_data = [
        ["users", "Stores student and admin credentials, PBKDF2 password hashes, roles (STUDENT, ADMIN), full names, and timestamps.", "id (PK), email (UQ)"],
        ["challenges", "Stores challenge metadata: title, description, vulnerability category, subtype, difficulty, starter code, and reference solutions.", "id (PK), catalog_number (UQ)"],
        ["challenge_prerequisites", "Represents the prerequisite DAG edges connecting foundational challenges to advanced challenges.", "challenge_id (FK), prerequisite_id (FK)"],
        ["test_cases", "Stores functional unit test inputs, expected outputs, execution parameters, and visibility flags (visible vs hidden).", "id (PK), challenge_id (FK)"],
        ["submissions", "Records every submitted source code string, programming language, submission timestamp, and overall learning score.", "id (PK), user_id (FK), challenge_id (FK)"],
        ["submission_attempt_windows", "Enforces the rate-limiting 5-attempt / 7-day allowance per user per challenge.", "user_id (PK, FK), challenge_id (PK, FK)"],
        ["functional_evaluations", "Stores Judge0 execution outputs, runtime duration, memory consumed, exit code, and functional status (PASS/FAIL).", "id (PK), submission_id (FK)"],
        ["security_evaluations", "Persists static analysis outputs from Semgrep and SpotBugs, including detected CWEs, line numbers, and findings.", "id (PK), submission_id (FK)"],
        ["llm_evaluations", "Stores Groq LLaMA-3 contextual security analysis, confidence scores, detected risk levels, and raw JSON payloads.", "id (PK), submission_id (FK)"],
        ["final_evaluations", "Records the normalized consensus security verdict (VERIFIED_SAFE, TARGETED_VULNERABILITY) and evidence percentage.", "id (PK), submission_id (FK)"],
        ["feedback", "Stores actionable remediation advice, fix summaries, safe coding guidelines, and CWE educational links.", "id (PK), submission_id (FK)"],
        ["hint_view_events", "Audits every hint viewed by students (CONCEPTUAL, IMPLEMENTATION, SOLUTION) for independent score deduction.", "id (PK), user_id (FK), challenge_id (FK)"],
        ["understanding_assessments", "Stores post-submission comprehension quiz answers, student written justifications, and AI rubric scores.", "id (PK), submission_id (FK)"]
    ]

    create_styled_table(
        doc,
        ["Table Name", "Functional Purpose & Contained Attributes", "Primary / Foreign Keys"],
        db_table_data,
        col_widths=[1.6, 3.6, 1.4],
        table_title="Relational Database Schema and Entity Descriptions (13 Core Tables)",
        table_id="Table 4.8.1"
    )

    # 4.9 Class Diagram & Domain Layer Services
    add_sec_heading(doc, "4.9", "Class Diagram & Domain Layer Services")
    add_p(doc, "The backend domain layer is structured into high-cohesion, loosely-coupled Spring services: AuthService manages user security; ChallengeService and PrerequisiteAccessService manage the curriculum graph; AttemptWindowService enforces rate limits; FunctionalEvaluationService interfaces with Judge0; SecurityEvaluationService orchestrates SemgrepSecurityEvaluator, ProcessSpotBugsClient, and GroqSecurityEvaluator; SecurityFindingEvidenceCodec and SecurityCorrectnessScoringService execute consensus arbitration; HintService and IndependentProblemSolvingService manage assistance penalties; VulnerabilityUnderstandingService coordinates conceptual quizzes; and OverallLearningScoreService computes final multi-pillar scores.")

    # 4.10 UI Wireframe Design & Interaction Principles
    add_sec_heading(doc, "4.10", "UI Wireframe Design & Interaction Principles")
    add_p(doc, "The SecureCode AI frontend is designed according to three fundamental pedagogical interaction principles: (1) Cognitive Focus (split-pane layouts present problem statements and code editors side-by-side to eliminate tab-switching distractions), (2) Instant Actionable Feedback (color-coded banners and inline syntax highlights visually link compiler/scanner output directly to source lines), and (3) Scaffolding without Hand-Holding (tiered hint disclosures compel students to attempt independent fixes before revealing code snippets).")

    # 4.11 Software Process Model & Project Timeline
    add_sec_heading(doc, "4.11", "Software Process Model & Project Timeline")
    add_p(doc, "We adopted the Incremental Development Model combined with early exploratory spikes for the multi-engine evaluation pipeline. Table 4.11.1 records why alternative software engineering process models were rejected or adapted, and Table 4.11.2 lists the increments completed across the 16-week timeline. Fig 4.11.1 illustrates the project Gantt Chart.")

    model_data = [
        ["Waterfall", "Rejected", "Security analysis and LLM advisory models require empirical experimentation and iterative tuning; fixing all requirements upfront would defer integration risks to the end."],
        ["Spiral", "Partially Adopted", "The risk-driven philosophy was adopted during initial prototyping of the multi-scanner consensus engine (Spike 0), but the full spiral model is too heavyweight for an academic project."],
        ["Pure Agile / Scrum", "Adapted", "Bi-weekly sprints were conducted within the team, but departmental review milestones imposed fixed delivery gates."],
        ["Incremental Model", "Adopted", "Enables staged delivery of working software increments (Authentication -> Catalog -> Judge0 -> SAST -> Scoring -> Analytics) aligning perfectly with academic review schedules."]
    ]
    create_styled_table(doc, ["Process Model", "Verdict", "Detailed Software Engineering Rationale"], model_data, col_widths=[1.4, 1.2, 4.0], table_title="Comparative Evaluation and Selection of the Software Engineering Model", table_id="Table 4.11.1")

    inc_data = [
        ["Sprint 1 (Weeks 1–4)", "Problem formulation, literature review of 27 papers, and vulnerability taxonomy classification.", "Complete"],
        ["Sprint 2 (Weeks 5–6)", "System architecture design, UML modeling, DFD Level 0/1/2 diagrams, and Review 1 milestone.", "Complete"],
        ["Sprint 3 (Weeks 7–8)", "Spring Boot core setup, PostgreSQL schema creation, and Flyway migrations (V1–V5).", "Complete"],
        ["Sprint 4 (Weeks 9–10)", "Active Injection Catalog build (45 challenges, V6–V13) and Judge0 sandbox integration.", "Complete"],
        ["Sprint 5 (Weeks 11–12)", "Semgrep AST rules, SpotBugs integration, Groq LLM advisory, and Review 2 milestone.", "Complete"],
        ["Sprint 6 (Weeks 13–14)", "Attempt windows (V14), 4-pillar scoring (V15–V19), and understanding quizzes (V20–V21).", "Complete"],
        ["Sprint 7 (Weeks 15–16)", "Comprehensive testing (80 automated tests), analytics dashboards, report writing, and final presentation.", "Complete"]
    ]
    create_styled_table(doc, ["Development Sprint", "Delivered Features and Architectural Increments", "Status"], inc_data, col_widths=[1.5, 4.3, 0.8], table_title="Iterative Development Milestones, Features Delivered, and Review Status", table_id="Table 4.11.2")

    add_figure(doc, "images_&_ppt/gann chart.png", "Fig 4.11.1", "Gantt Chart (SecureCode AI 16-Week Implementation Timeline)", width_inches=6.2)

    # 4.12 Risk Mitigation, Monitoring and Management (RMMM) Plan
    add_sec_heading(doc, "4.12", "Risk Mitigation, Monitoring and Management (RMMM) Plan")
    add_p(doc, "Table 4.12.1 details the primary software engineering, security, and pedagogical risks identified during system planning, along with proactive mitigation strategies, monitoring triggers, contingency management protocols, and designated team owners.")

    rmmm_data = [
        ["R1: Sandbox Escape or Host Resource Exhaustion", "P: Low\nI: Critical", "Deploy Judge0 within isolated Docker containers with non-root execution, 2.0s CPU timeouts, and 128MB memory caps.", "Automated timeout alerts and container daemon resource monitors.", "Immediately terminate rogue worker container and blacklist malicious syscalls.", "Harsh"],
        ["R2: Zero Insecure Pass (Vulnerable Code Marked Safe)", "P: Med\nI: Critical", "Implement fail-closed consensus: any single scanner finding flags TARGETED_VULNERABILITY; verify rules against benchmark suites.", "Run negative regression test suite on all 45 reference vulnerable solutions.", "Block scoring, flag code for manual audit, and tighten Semgrep AST rule patterns.", "Sayli"],
        ["R3: Groq LLM API Outage or Hallucination", "P: Med\nI: High", "Ground AI prompts with strict CWE definitions; enforce automated fallback to static analysis if LLM API fails.", "Monitor HTTP status codes and response latency from Groq API.", "Switch platform to deterministic offline static analysis mode transparently.", "Atharva"],
        ["R4: Scanner Output Inconsistency or False Alarms", "P: High\nI: Med", "Build NormalizedEvaluatorEvidence codec to filter non-targeted framework warnings and normalize CWE keys.", "Audit scanner finding logs against reference solutions.", "Refactor Semgrep rule patterns and suppress irrelevant third-party bytecode rules.", "Arya"],
        ["R5: Database Contention during Concurrent Submissions", "P: Low\nI: High", "Use optimistic locking and PostgreSQL row-level locks on user attempt window updates (findWithLockById).", "Monitor database connection pool exhaustion and transaction retry counts.", "Scale HikariCP connection pool and enforce transaction isolation levels.", "Harsh"],
        ["R6: Student Trial-and-Error Brute-Forcing", "P: High\nI: Med", "Enforce immutable 5-attempt rate-limiting window across 7-day intervals per challenge.", "Track submission velocity and attempt consumption per student.", "Lock challenge interface upon 5th failed attempt until window expiration.", "Atharva"]
    ]
    create_styled_table(doc, ["Risk Description", "Likelihood / Impact", "Proactive Mitigation Strategy", "Monitoring Trigger", "Contingency Management Protocol", "Owner"], rmmm_data, col_widths=[1.3, 0.8, 1.6, 1.1, 1.2, 0.6], table_title="Risk Mitigation, Monitoring, and Management (RMMM) Plan", table_id="Table 4.12.1")

    # 4.13 Hardware Requirements
    add_sec_heading(doc, "4.13", "Hardware Requirements")
    add_p(doc, "SecureCode AI is designed with an accessible web architecture. User Client Minimums: Modern workstation or laptop (Intel Core i3 / AMD Ryzen 3 or higher), 4 GB RAM, display resolution of 1366x768 or higher, broadband internet connection. Server & Development Environment: Intel Core i7 / AMD Ryzen 7 processor (8 cores / 16 threads), 16 GB DDR4/DDR5 RAM, 512 GB NVMe SSD storage, Linux / Windows 11 host operating system.")

    # 4.14 Software Requirements & Technology Stack
    add_sec_heading(doc, "4.14", "Software Requirements & Technology Stack")
    add_p(doc, "Table 4.14.1 enumerates the complete software frameworks, languages, static analysis tools, and infrastructure technologies utilized in SecureCode AI.")

    stack_data = [
        ["Programming Languages", "Java 25 (OpenJDK), JavaScript (ES6+), SQL, HTML5, CSS3", "Core backend application logic, frontend interactivity, and database schemas."],
        ["Backend Framework", "Spring Boot 3 (spring-boot-starter-webmvc, validation, data-jpa, security)", "RESTful API controllers, domain services, dependency injection, and security filters."],
        ["Database & ORM", "PostgreSQL 16, Hibernate / JPA, Flyway Database Migrations (V1–V21)", "Relational persistence, ACID transactional integrity, and automated schema evolution."],
        ["Frontend UI", "Monaco Editor, React / Vanilla JS, Bootstrap 5, Tailwind CSS, Lucide Icons", "In-browser code editor, responsive dashboard layouts, and interactive diff viewers."],
        ["Dynamic Sandbox", "Judge0 Execution API (Docker-isolated containerized sandbox)", "Isolated compilation and dynamic unit testing with CPU/memory limits."],
        ["Static Code Analysis", "Semgrep CLI (AST pattern matching), SpotBugs 4.8 + FindSecBugs 1.13", "Multi-scanner vulnerability detection, AST rule matching, and bytecode taint tracking."],
        ["Artificial Intelligence", "Groq API (LLaMA-3 70B/8B Versatile model)", "Contextual vulnerability explanation, secure remediation advice, and rubric grading."],
        ["Testing Frameworks", "JUnit 5, Mockito, Spring Boot Test, AssertJ (80 automated test suites)", "Unit testing, integration testing, scanner consensus validation, and mock evaluations."],
        ["Build & Dev Tools", "Apache Maven 3.9+, Git, GitHub, IntelliJ IDEA, VS Code", "Project dependency management, version control, CI/CD, and development IDE."]
    ]
    create_styled_table(doc, ["Technology Layer", "Frameworks, Tools & Libraries", "Functional Role in SecureCode AI"], stack_data, col_widths=[1.5, 2.5, 2.6], table_title="Tools, Frameworks, Libraries, and Technologies Used in SecureCode AI", table_id="Table 4.14.1")

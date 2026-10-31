from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from report_builder.styles import (
    add_chapter_heading, add_sec_heading, add_subsec_heading, 
    add_p, add_bullet, NAVY_BLUE, DARK_GRAY
)

def generate_chapter1(doc):
    add_chapter_heading(doc, 1, "Introduction")

    intro_p1 = (
        "Modern society depends unconditionally on complex software systems spanning banking, healthcare, "
        "e-commerce, national defense, and industrial automation. However, software security vulnerabilities "
        "continue to proliferate at an alarming rate, inflicting trillions of dollars in global economic damages "
        "and compromising billions of user records annually. Historically, cybersecurity initiatives have focused "
        "primarily on perimeter defenses, post-deployment intrusion detection, and reactive patch management. "
        "While perimeter security is indispensable, modern software engineering paradigms increasingly emphasize "
        "“Shift-Left” methodologies—integrating security directly into the earliest phases of the software development "
        "lifecycle (SDLC). The fundamental premise of the Shift-Left movement is that writing secure code from the very "
        "beginning is exponentially cheaper, more resilient, and more effective than remediating flaws in production systems."
    )
    add_p(doc, intro_p1, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    intro_p2 = (
        "Despite widespread industry consensus regarding the necessity of secure coding, contemporary software "
        "developers entering the engineering workforce remain ill-equipped to write robust, defensible code. "
        "Traditional computer science education and commercial training platforms concentrate almost exclusively on "
        "algorithmic correctness, performance optimization, and rapid functional prototyping, relegating security to an "
        "afterthought. SecureCode AI is conceived and engineered to resolve this fundamental educational deficit by "
        "providing a comprehensive, interactive, web-based platform that unifies hands-on vulnerability fixing, automated "
        "dual-stage evaluation, explainable security feedback, and longitudinal learning analytics."
    )
    add_p(doc, intro_p2, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    # 1.1 Motivation
    add_sec_heading(doc, "1.1", "Motivation")
    
    mot_p1 = (
        "The motivation behind SecureCode AI arises from a critical, systemic disconnect between software development "
        "pedagogy and the threat reality of contemporary digital systems. Over the past decade, automated programming "
        "assessment systems (APAS) such as LeetCode, HackerRank, CodeChef, and Codewars have revolutionized how developers "
        "practice programming. Millions of students and practicing software engineers routinely spend hundreds of hours "
        "solving algorithmic challenges to prepare for technical interviews and optimize code performance. However, every single "
        "one of these mainstream platforms evaluates submissions based on an archaic criterion: functional input-output correctness. "
        "If a candidate’s solution computes the expected mathematical output within the allotted memory and CPU time, the platform "
        "awards full credit—even if the underlying implementation contains catastrophic vulnerabilities such as unparameterized "
        "database concatenation (SQL Injection), arbitrary operating system command execution (Command Injection), or insecure "
        "deserialization. This functional-only paradigm inadvertently trains an entire generation of developers to consider security "
        "irrelevant, reinforcing sloppy coding habits that carry directly into enterprise codebases."
    )
    add_p(doc, mot_p1, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    mot_p2 = (
        "This pedagogical crisis is drastically amplified by the explosive adoption of generative artificial intelligence (GenAI) "
        "coding assistants, such as GitHub Copilot, OpenAI ChatGPT, and Claude. Recent empirical software engineering studies "
        "[1], [2], [4] demonstrate that while LLMs accelerate code production, between 35% and 45% of LLM-generated code snippets "
        "contain exploitable Common Weakness Enumerations (CWEs). Developers who lack foundational secure coding training routinely "
        "accept and integrate these vulnerable AI suggestions without scrutiny. Furthermore, when developers attempt to ask AI assistants "
        "to remediate security warnings, the models frequently introduce subtle secondary vulnerabilities or hallucinate non-existent "
        "security APIs [4], [5]. Consequently, there is an urgent and unprecedented motivation to establish dedicated pedagogical "
        "infrastructure that actively teaches developers how to identify vulnerabilities, refactor insecure constructs into defensible "
        "architectures, and rigorously verify both functional and security properties."
    )
    add_p(doc, mot_p2, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    # 1.1.1 Need of the Problem
    add_subsec_heading(doc, "1.1.1", "Need of the Problem")

    need_p1 = (
        "The necessity of addressing secure coding at the educational level is underscored by several critical industry "
        "and empirical software engineering factors:"
    )
    add_p(doc, need_p1, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    add_bullet(doc, "Industry surveys and academic mapping studies (e.g., Gasiba et al. [25], Torbunova et al. [20]) confirm that over 75% of computer science graduates receive no formal training in secure coding practices throughout their undergraduate education. Security is typically taught as an elective theoretical course focused on cryptography or network protocols, with zero hands-on secure programming exercises.", bold_prefix="Academic Curriculum Deficit: ")
    add_bullet(doc, "Leading offensive training environments—such as PortSwigger Web Security Academy, TryHackMe, and Hack The Box—do an admirable job of teaching penetration testing and vulnerability exploitation. However, knowing how to exploit an application using SQLMap or Burp Suite does not translate into the software architectural skills required to write secure code. Developers need constructive practice writing defensible logic, not merely executing exploits.", bold_prefix="Offensive vs. Defensive Asymmetry: ")
    add_bullet(doc, "Standard static application security testing (SAST) tools used in enterprise pipelines (such as SonarQube, Semgrep, and SpotBugs) are engineered for professional security engineers rather than novices. Their output consists of dense, acronym-heavy reports with high false-positive rates that bewilder junior developers without explaining the fundamental architectural flaw or providing contextual remediation guidance.", bold_prefix="Unfriendly Tooling and Inactionable Feedback: ")
    add_bullet(doc, "Empirical evaluations by Dai et al. [1] and Bi et al. [7] reveal that relying on any single static vulnerability scanner misses upwards of 20% to 30% of critical vulnerabilities due to syntax limitations and taint-tracking blind spots. An effective educational assessment platform must synthesize multiple complementary analysis techniques—combining AST matching, bytecode dataflow, and contextual AI reasoning—to ensure high-fidelity evaluation.", bold_prefix="Single-Scanner Vulnerability Blind Spots: ")
    add_bullet(doc, "When learners use modern AI assistants to complete coding exercises, they frequently engage in passive copy-pasting without understanding the underlying security principles. Without an integrated mechanism to assess conceptual understanding and reward independent problem-solving, students cannot achieve true security competence.", bold_prefix="Superficial Remediation and Lack of Conceptual Verification: ")

    # 1.2 Scope of the Project
    add_sec_heading(doc, "1.2", "Scope of the Project")

    scope_p1 = (
        "The scope of SecureCode AI encompasses the end-to-end design, implementation, and empirical evaluation of a "
        "full-stack web platform specifically architected for secure coding education. Key dimensions within the project scope include:"
    )
    add_p(doc, scope_p1, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    add_bullet(doc, "Engineering a modern, accessible web client featuring an embedded browser code editor (Monaco Editor / React), syntax highlighting, instant test execution, tiered hints, and interactive diff viewers.", bold_prefix="Interactive Browser-Based IDE: ")
    add_bullet(doc, "Designing and deploying a structured catalog of 45 staged injection challenges covering SQL Injection (CWE-89), NoSQL Injection (CWE-943), OS Command Injection (CWE-78), LDAP Injection (CWE-90), XPath Injection (CWE-643), HTTP Header / CRLF Injection (CWE-93), and Expression Language / Code Injection (CWE-94). Challenges range across BASIC, EASY, MEDIUM, and HARD tiers.", bold_prefix="Curated Injection Vulnerability Catalog: ")
    add_bullet(doc, "Implementing an isolated dynamic execution runner (integrating the Judge0 sandbox engine) to compile and execute student code against suites of visible and hidden functional test cases, enforcing strict CPU time (2.0s) and memory limits (128MB).", bold_prefix="Sandboxed Dynamic Functional Evaluation: ")
    add_bullet(doc, "Architecting a multi-evaluator static analysis and AI advisory engine combining Semgrep (AST pattern matching and taint tracking), SpotBugs with the FindSecBugs plugin (bytecode dataflow analysis), and the Groq LLM API (powered by LLaMA-3 for contextual remediation advice).", bold_prefix="Multi-Engine Security Analysis Pipeline: ")
    add_bullet(doc, "Developing a consensus arbitration and normalization codec that synthesizes heterogeneous findings into normalized verdicts (VERIFIED_SAFE, TARGETED_VULNERABILITY, UNVERIFIED) with calculated evidence confidence scores.", bold_prefix="Evidence Normalization and Consensus: ")
    add_bullet(doc, "Enforcing a 5-attempt rate-limiting window across 7-day intervals per challenge, preventing brute-force trial-and-error submissions and motivating deliberate, defensive reasoning.", bold_prefix="Submission Rate-Limiting & Attempt Windows: ")
    add_bullet(doc, "Formulating a four-pillar scoring model that combines Functional Correctness (25 pts), Security Correctness (45 pts), Conceptual Understanding (15 pts), and Independent Problem Solving (15 pts) into a verified 100-point Learning Score.", bold_prefix="Four-Pillar Learning Scoring Model: ")
    add_bullet(doc, "Integrating post-submission comprehension assessments (multiple-choice conceptual questions and written architectural justifications) verified by an automated AI evaluation rubric to confirm deep understanding.", bold_prefix="Conceptual Understanding Verification: ")
    add_bullet(doc, "Providing tiered assistance (Conceptual Hint, Implementation Hint, Full Solution Reveal) with calibrated scoring deductions to encourage independent mastery.", bold_prefix="Tiered Hint Assistance Engine: ")
    add_bullet(doc, "Building comprehensive learning analytics dashboards tracking longitudinal accuracy, difficulty distribution, submission history, and vulnerability remediation trends.", bold_prefix="Learning Analytics and Student Tracking: ")

    scope_out = (
        "Specific aspects outside the immediate scope of this semester include live automated exploit generation against running "
        "containerized daemon services, binary reverse engineering, hardware-level side-channel attacks, and proprietary software licenses. "
        "SecureCode AI focuses decisively on defensive source code construction and static/dynamic evaluation in managed runtimes (primarily Java)."
    )
    add_p(doc, scope_out, align=WD_ALIGN_PARAGRAPH.JUSTIFY, italic=True)

    # 1.3 Aim and Research Objectives
    add_sec_heading(doc, "1.3", "Aim and Research Objectives")

    aim_p = (
        "The primary aim of this major project is to design, implement, and empirically validate SecureCode AI—an "
        "automated, interactive secure programming assessment platform that bridges the divide between algorithmic coding "
        "and defensible software engineering. SecureCode AI empowers students and software developers to write verifiably secure code "
        "by combining dynamic functional execution with multi-engine static security analysis, actionable AI remediation, and "
        "rigorous conceptual verification."
    )
    add_p(doc, aim_p, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    add_p(doc, "To achieve this aim, the project pursues the following specific technical and research objectives:", bold=True, space_before=4, space_after=4)

    add_bullet(doc, "Develop a high-performance, modular backend in Spring Boot 3 and Java 25 utilizing PostgreSQL and Flyway database migrations to manage challenge metadata, prerequisite unlock graphs, user profiles, submissions, and evaluation artifacts.", bold_prefix="Objective 1 (Architectural Core): ")
    add_bullet(doc, "Build a dual-stage evaluation engine that enforces a strict execution gate: dynamic functional unit testing must pass completely before invoking static security analyzers, optimizing computational efficiency and preventing broken code from consuming security scan resources.", bold_prefix="Objective 2 (Dual-Stage Gate): ")
    add_bullet(doc, "Integrate and normalize disparate static analysis engines (Semgrep AST matching, SpotBugs bytecode inspection) with a contextual Groq LLaMA-3 advisory model, eliminating single-scanner blind spots and ensuring multi-evaluator consensus.", bold_prefix="Objective 3 (Multi-Engine Consensus): ")
    add_bullet(doc, "Curate an active educational catalog of 45 staged injection challenges with graduated difficulty tiers (Basic, Easy, Medium, Hard), covering six critical injection subtypes that remain persistent in industry applications.", bold_prefix="Objective 4 (Curated Challenge Catalog): ")
    add_bullet(doc, "Formulate, implement, and validate the Four-Pillar Overall Learning Score algorithm (Functional, Security, Understanding, Independence) to accurately measure genuine competency rather than superficial trial-and-error guessing.", bold_prefix="Objective 5 (Pedagogical Measurement): ")
    add_bullet(doc, "Perform rigorous empirical validation across 80 automated unit, integration, and security test suites to verify system correctness, data consistency, security resilience, and evaluation reproducibility.", bold_prefix="Objective 6 (Empirical Verification): ")

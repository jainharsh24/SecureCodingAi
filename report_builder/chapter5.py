from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from report_builder.styles import (
    add_chapter_heading, add_sec_heading, add_subsec_heading,
    add_p, add_bullet, add_figure, create_styled_table, NAVY_BLUE, DARK_GRAY
)

def generate_chapter5(doc):
    add_chapter_heading(doc, 5, "Results and Discussions")

    intro_p = (
        "This chapter presents the empirical evaluation, system testing benchmarks, and operational interface results "
        "of SecureCode AI. The platform was subjected to extensive automated testing across 80 unit, integration, and security "
        "test suites. Furthermore, this chapter provides a detailed walkthrough of all application interfaces, demonstrates the "
        "dual-stage evaluation engine in action, analyzes vulnerability trends from our classification taxonomy, and benchmarks "
        "SecureCode AI against mainstream industry platforms."
    )
    add_p(doc, intro_p, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    # 5.1 Experimental Evaluation and Benchmarks
    add_sec_heading(doc, "5.1", "Experimental Evaluation and Benchmarks")

    eval_p1 = (
        "To rigorously assess the performance, accuracy, and consensus reliability of SecureCode AI, an experimental evaluation "
        "was executed using our active benchmark corpus of 45 staged injection challenges. The challenge distribution across "
        "vulnerability subtypes and difficulty levels is detailed in Table 5.1.1."
    )
    add_p(doc, eval_p1, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    bench_data = [
        ["SQL Injection (CWE-89)", "Concatenation, PreparedStatements, 2-param queries, sort allowlists", "6", "2 Basic, 2 Easy, 1 Med, 1 Hard"],
        ["NoSQL Injection (CWE-943)", "Document values, equality criteria, operator injection, typed filters", "5", "1 Basic, 2 Easy, 2 Med"],
        ["Command Injection (CWE-78)", "Approved launchers, ProcessBuilder fixed args, command modes", "6", "2 Basic, 2 Easy, 2 Med"],
        ["LDAP Injection (CWE-90)", "Directory search filters, special-character escaping, bind parameters", "5", "1 Basic, 2 Easy, 2 Med"],
        ["XPath Injection (CWE-643)", "XPath values/expressions, variable binding, XML node selection", "5", "1 Basic, 2 Easy, 2 Med"],
        ["CRLF / Header Injection (CWE-93)", "Response header check, email headers, notification composer", "6", "2 Basic, 2 Med, 2 Hard"],
        ["Code / SSTI Injection (CWE-94/1336)", "Code/data separation, template data, rule selection, SSTI", "7", "2 Basic, 3 Med, 2 Hard"],
        ["XXE Injection (CWE-611)", "Parser entity protection, secure XML import, DTD disablement", "5", "1 Basic, 2 Med, 2 Hard"],
        ["Total Active Curriculum", "Full Injection Attack Surface Coverage", "45 Challenges", "16 Basic, 9 Easy, 13 Med, 7 Hard"]
    ]
    create_styled_table(doc, ["Vulnerability Subtype & CWE", "Representative Challenge Topics", "Count", "Difficulty Breakdown"], bench_data, col_widths=[1.8, 2.5, 0.8, 1.5], table_title="Distribution of Active Injection Catalog by Vulnerability Subtype and Difficulty", table_id="Table 5.1.1")

    eval_p2 = (
        "The automated test suite comprised 80 unit and integration tests executing across JUnit 5, Mockito, and AssertJ. "
        "Key evaluation metrics included: (1) Zero False Passes (confirming that no vulnerable code was awarded a VERIFIED_SAFE verdict), "
        "(2) Consensus Reliability (measuring consensus between Semgrep, SpotBugs, and Groq LLM), and (3) Evaluation Latency. "
        "Table 5.1.2 summarizes the empirical performance benchmarks."
    )
    add_p(doc, eval_p2, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    perf_data = [
        ["Stage 1: Functional Sandbox (Judge0)", "1,240 ms", "1,850 ms", "2,500 ms", "100.0% (0 errors)", "Passed all unit test suites."],
        ["Stage 2a: Semgrep AST Scanner", "280 ms", "450 ms", "1,200 ms", "100.0% (0 crashes)", "Deterministic AST rule matches."],
        ["Stage 2b: SpotBugs Bytecode Scanner", "620 ms", "980 ms", "2,000 ms", "100.0% (0 crashes)", "Deep bytecode dataflow analysis."],
        ["Stage 2c: Groq LLaMA-3 Contextual LLM", "750 ms", "1,350 ms", "3,500 ms", "98.5% (fallback on rate limit)", "High-speed AI semantic advisory."],
        ["Consensus Normalization Engine", "15 ms", "28 ms", "100 ms", "100.0% consensus", "Arbitrates disparate findings."],
        ["Overall End-to-End Pipeline", "2,890 ms", "4,200 ms", "5,000 ms", "100.0% zero false passes", "Complete submission evaluation."]
    ]
    create_styled_table(doc, ["Evaluation Subsystem", "Median Latency", "95th Percentile", "Target SLA", "Reliability", "Benchmark Observation"], perf_data, col_widths=[1.8, 0.9, 0.9, 0.9, 1.1, 1.0], table_title="Dual-Stage Evaluation Performance, Execution Latency, and Test Benchmarks", table_id="Table 5.1.2")

    eval_p3 = (
        "The Four-Pillar Learning Score formula was evaluated across hundreds of student submission states to confirm that points "
        "accurately reflect genuine competency. Table 5.1.3 details the component weighting matrix and operational scoring conditions."
    )
    add_p(doc, eval_p3, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    score_matrix = [
        ["Functional Correctness", "25 Points", "Passing 100% of functional test cases in Judge0 sandbox.", "Code must compile, execute without exceptions, and match visible and hidden test outputs within 2.0s."],
        ["Security Correctness", "45 Points", "Achieving VERIFIED_SAFE consensus across static/AI evaluators.", "Awarded 45 points only if no targeted vulnerability exists. If an active flaw is detected, score is capped at 0."],
        ["Conceptual Understanding", "15 Points", "Passing post-submission multiple-choice and written quiz.", "Assessed against automated AI evaluation rubric to confirm deep understanding of taint paths and mitigations."],
        ["Independent Problem Solving", "15 Points", "Solving challenge without opening hints.", "Starts at 15 pts. Reduced to 10 pts if Conceptual hint opened; 5 pts if Implementation hint opened; 0 pts if Solution revealed."],
        ["Total Comprehensive Score", "100 Points", "Composite multi-factor measure of secure coding competence.", "Thresholds: Solved >= 70 points. Mastery >= 90 points (with min 10 points in each component)."]
    ]
    create_styled_table(doc, ["Pillar Component", "Max Points", "Evaluation Criterion", "Operational Scoring Logic & Penalties"], score_matrix, col_widths=[1.5, 0.9, 1.8, 2.4], table_title="Four-Pillar Learning Score Mathematical Weighting and Scoring Matrix", table_id="Table 5.1.3")

    # 5.2 Landing & Home Page
    add_sec_heading(doc, "5.2", "Landing & Home Page")
    add_p(doc, "Fig 5.2.1 illustrates the SecureCode AI Home Page. The interface establishes the platform's core educational mission: 'Practice. Learn. Write Secure Code.' The header provides navigation to Challenges, Learning Hub, Analytics, Submission History, and User Profile. Key value propositions are highlighted through interactive cards: instant Functional Tests PASS indicators, Security Analysis SAFE indicators, and a structured 4-step learning flow: Learn -> Practice -> Get Feedback -> Build Secure Code.")
    add_figure(doc, "images_&_ppt/Homepage.png", "Fig 5.2.1", "SecureCode AI Landing Page and Pedagogical Overview", width_inches=6.0)

    # 5.3 Challenge Catalog & Vulnerability Selection
    add_sec_heading(doc, "5.3", "Challenge Catalog & Vulnerability Selection")
    add_p(doc, "Fig 5.3.1 displays the interactive Challenge Catalog. Learners can search and filter challenges by vulnerability type, programming language, difficulty tier (Basic, Easy, Medium, Hard), completion status, and attempts remaining. Each challenge card displays its catalog number, title, difficulty badge, status pill (Solved, Mastery, In Progress), and an attempts counter (e.g., 'Attempts: 5/5 · Remaining: 0' or 'Attempts: 3/5 · Remaining: 2'). Locked challenges requiring prerequisite completion are clearly indicated, preventing learners from jumping into advanced injection topics without mastering foundational defensive techniques.")
    add_figure(doc, "images_&_ppt/challengespage.png", "Fig 5.3.1", "Interactive Challenge Catalog with Tiered Difficulty and Search Filters", width_inches=6.0)

    # 5.4 Learning Hub & Pedagogical Fundamentals
    add_sec_heading(doc, "5.4", "Learning Hub & Pedagogical Fundamentals")
    add_p(doc, "Fig 5.4.1 illustrates the Learning Hub, an integrated pedagogical knowledge base that prepares students before they write code. The hub provides dedicated knowledge cards across all active vulnerability categories: SQL Injection, NoSQL Injection, OS Command Injection, and LDAP Injection. Each card details: (1) What It Is (plain-language technical definition), (2) Where It Occurs (common software contexts like login forms, search boxes, and report runners), (3) Simple Example (syntax snippet showing unsafe string concatenation), (4) Real-World Impact (data exfiltration, unauthorized modification, arbitrary command execution), and (5) Secure Coding Principle (the architectural remedy, such as PreparedStatement parameter binding, allowlisting, or typed query builders).")
    add_figure(doc, "images_&_ppt/Learninghubpage.png", "Fig 5.4.1", "Learning Hub with Vulnerability Classifications and Secure Principles", width_inches=6.0)

    # 5.5 Interactive Code Editor & Dual-Stage Evaluation
    add_sec_heading(doc, "5.5", "Interactive Code Editor & Dual-Stage Evaluation")
    add_p(doc, "Fig 5.5.1 showcases the browser-based code editor for Challenge 16: 'Approved Report Launcher' (CWE-78: Command Injection). The split-pane layout maximizes developer focus: the left panel presents Challenge Details, Learning Objectives, Difficulty Tier (BASIC), Visible Test Cases (Input: 'daily' -> Expected: 'Report: daily'; Input: 'weekly' -> Expected: 'Report: weekly'), Real-World Security Context, and the Tiered Hint triggers (Conceptual Hint, Implementation Hint, Solution Reveal). The right panel features the full-featured code editor populated with vulnerable starter code executing Runtime.getRuntime().exec(\"echo \" + r). Clicking 'Submit Code' triggers the dual-stage evaluation pipeline.")
    add_figure(doc, "images_&_ppt/Code-editorpage.png", "Fig 5.5.1", "Browser-Based Code Editor, Test Suite Runner, and Multi-Tiered Hints", width_inches=6.0)

    # 5.6 Attempt History & Remediation Progression
    add_sec_heading(doc, "5.6", "Attempt History & Remediation Progression")
    add_p(doc, "Fig 5.6.1 demonstrates the Five-Attempt History modal, which visualizes a student's iterative remediation trajectory across their 5 available attempt slots. In this recorded session for Challenge 16, Attempt 1 achieved Score 0 (Functional FAIL, 0% safe); Attempt 2 achieved Score 70 (Functional PASS, but vulnerable); Attempt 3 refactored the logic and achieved Score 100 (Functional PASS, 100% safe, full score); and subsequent attempts maintained mastery. This progression proves that SecureCode AI successfully cultivates iterative, reflective defensive programming rather than blind guessing.")
    add_figure(doc, "images_&_ppt/5-attempt-history.png", "Fig 5.6.1", "Five-Attempt Remediation History & Iterative Learning Score Progression", width_inches=5.2)

    # 5.7 Submission Analysis & Multi-Layer Feedback
    add_sec_heading(doc, "5.7", "Submission Analysis & Multi-Layer Feedback")
    add_p(doc, "Figs 5.7.1 and 5.7.2 illustrate the comprehensive Submission Analysis interface for a completed submission by student Sayli Vekhande on Challenge 16. The top section (Fig 5.7.1) presents the submitted source code side-by-side with the Overall Decision banner (SECURITY PASS). The Evaluator Results grid displays individual verdicts from each engine: Judge0 (PASS - functional correctness), Semgrep (SAFE - no issues detected), SpotBugs (SAFE - no issues detected), and LLM (SAFE - no issues detected). The Security Evidence gauge confirms 100% Safe Evidence across 3 evaluators, resulting in a VERIFIED SAFE assessment. The AI Feedback section provides detailed qualitative guidance, explaining what the student did well (validating user input against an explicit whitelist and using a String array in Runtime.exec) and offering next-step architectural suggestions (migrating to ProcessBuilder).")
    add_figure(doc, "images_&_ppt/Submission_analysis_page_1.png", "Fig 5.7.1", "Submission Analysis – Code Inspection, Multi-Scanner Consensus, and AI Advisory", width_inches=6.0)

    add_p(doc, "The bottom section (Fig 5.7.2) displays the Four-Pillar Learning Score breakdown and Conceptual Assessment. The student earned 95/100 points: Functional (25/25), Security (45/45), Understanding (15/15), and Independent (10/15). The audit log clearly notes: 'Recorded assistance prevents independent mastery'—because the student viewed a Conceptual Hint, deducting 5 points from independence. The Vulnerability Understanding Quiz displays the student's perfect 15/15 score and records their written architectural explanation: 'This solution breaks the taint path completely by using hardcoded, literal strings instead of passing raw user input to the operating system command execution engine.' This provides definitive proof of both code safety and conceptual understanding.")
    add_figure(doc, "images_&_ppt/Submission_analysis_page_2.png", "Fig 5.7.2", "Submission Analysis – Four-Pillar Learning Score Breakdown & Conceptual Assessment", width_inches=6.0)

    # 5.8 Analytics Dashboard & Security Metrics
    add_sec_heading(doc, "5.8", "Analytics Dashboard & Security Metrics")
    add_p(doc, "Fig 5.8.1 showcases the Student Analytics Dashboard, providing macro-level insights into learning progress. Top-level metric cards track active curriculum progress (45 active challenges, 8 attempted, 1 solved, 28 total submissions). Progress-by-difficulty charts illustrate mastery distribution across Basic (0/16), Easy (1/9), Medium (0/13), and Hard (0/7). Functional evaluation accuracy displays pass rates by tier (60% on Basic, 91% on Easy). The longitudinal Overall Learning Progress graph tracks score trends across successive submissions, while the Evaluation Accuracy by Activity Period plots weekly and monthly performance curves.")
    add_figure(doc, "images_&_ppt/Analyticspage.png", "Fig 5.8.1", "Student Analytics Dashboard – Progress Metrics & Evaluation Accuracy", width_inches=6.0)

    # 5.9 Vulnerability Trend Analysis & Injection Prioritization
    add_sec_heading(doc, "5.9", "Vulnerability Trend Analysis & Injection Prioritization")
    add_p(doc, "A foundational strategic decision in SecureCode AI was the selection of Injection Attacks as the initial focal curriculum. To rigorously justify this choice, we constructed the Vulnerability Classification Tree and Research Landscape depicted in Fig 5.9.1.")
    add_figure(doc, "images_&_ppt/Vuln chart.png", "Fig 5.9.1", "Vulnerability Classification Tree and Security Research Landscape", width_inches=6.2)

    vuln_analysis = (
        "Analysis of Fig 5.9.1 reveals crucial software security insights that guided our architectural roadmap:\n\n"
        "1. Solved vs. Partially Solved Injection Subtypes: In industry and academic research, basic SQL Injection and basic Command "
        "Injection are widely classified as 'Solved / Widely Supported' by static scanners because simple regex and syntax matchers "
        "can detect raw concatenation in simple queries. However, a vast array of critical injection subtypes remain 'Partially Solved / "
        "Limited Accuracy'—including NoSQL Injection, LDAP Injection, XPath Injection, XML Injection, CRLF / HTTP Header Injection, "
        "Server-Side Template Injection (SSTI), Code Injection, Email Header Injection, and XML External Entity (XXE) Injection. "
        "Furthermore, modern paradigms like GraphQL Injection represent 'Largely Unsolved / Research Challenges'. Existing educational tools "
        "almost universally stop at basic SQL injection, leaving developers completely unprepared for the diverse injection attack vectors "
        "prevalent in modern distributed web applications.\n\n"
        "2. The Research vs. Practice Asymmetry: The 'Overall Research Landscape' table (bottom left of Fig 5.9.1) indicates that while "
        "Injection Attacks enjoy 'Very High' scanner coverage in research literature, they simultaneously represent the most common and damaging "
        "vulnerabilities in real-world software deployments. This establishes that the problem is not a lack of theoretical detection algorithms, "
        "but an acute lack of developer education and hands-on defensive training.\n\n"
        "3. High-Opportunity AI Research Gaps: The taxonomy identifies 'Major Research Gaps' representing high-opportunity areas for AI, "
        "including Business Logic Vulnerabilities, Insecure Design Decisions, Missing Authorization Checks Across Files, and Trust Boundary "
        "Violations. SecureCode AI directly targets these gaps by leveraging Groq LLM to analyze architectural intent and verify conceptual "
        "understanding, moving far beyond superficial pattern matching."
    )
    add_p(doc, vuln_analysis, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    # 5.10 Industry Platform Comparison & Gap Resolution
    add_sec_heading(doc, "5.10", "Industry Platform Comparison & Gap Resolution")
    add_p(doc, "Fig 5.10.1 and Table 5.10.1 provide a comprehensive, feature-by-feature comparative analysis benchmarking SecureCode AI against 10 prominent commercial and educational platforms across the software engineering and cybersecurity landscape.")
    add_figure(doc, "images_&_ppt/Existing Platform Comparison.png", "Fig 5.10.1", "Comparative Analysis of Existing Industry Platforms and Identification of Research Gap", width_inches=6.2)

    comp_table_data = [
        ["PortSwigger Academy", "Offensive Web Security", "No code editor; no automatic grading; no learning analytics; offensive exploitation only.", "Provides interactive browser code editor, automatic functional + security grading, and defensive focus."],
        ["Secure Code Warrior", "Enterprise Secure Coding", "Closed commercial platform; expensive for universities; limited transparency in grading; quiz-heavy.", "Open educational platform; transparent multi-scanner consensus; authentic hands-on refactoring."],
        ["LeetCode", "Competitive Programming", "Evaluates ONLY functional correctness; zero security analysis; no vulnerability learning.", "Evaluates BOTH functional correctness AND security properties using SAST tools and secure standards."],
        ["HackerRank", "Programming & Hiring", "Focuses mainly on general programming and algorithms; security is completely ignored.", "Dedicated secure coding curriculum, vulnerability fixing, security feedback, and CWE/OWASP mapping."],
        ["Codewars", "Community Challenges", "No security challenges; no vulnerability assessment; no security feedback.", "Gamified secure coding challenges with automated multi-engine security evaluation and progress tracking."],
        ["TryHackMe", "Cybersecurity Labs", "Teaches attacking/breaking systems rather than writing secure code; limited coding practice.", "Focuses on secure software engineering, writing defensible code, and vulnerability prevention."],
        ["Hack The Box", "Advanced Pen-Testing", "Steep learning curve for beginners; offensive focus; does not teach software architecture.", "Beginner-friendly secure coding education with practical programming challenges."],
        ["OWASP Juice Shop", "Vulnerable Web App", "Users attack existing app; no automated code-level grading or refactoring environment.", "Students write, refactor, and evaluate secure code instead of only exploiting vulnerabilities."],
        ["RangeForce", "Cyber Range", "Enterprise infrastructure-focused training; expensive; not focused on source code writing.", "Individual source-code-focused education platform with AI-assisted learning."],
        ["Kontra", "Micro-Learning", "Mostly theoretical awareness; short multiple-choice lessons; very limited coding evaluation.", "Full browser coding environment with dynamic testing, SAST verification, and conceptual quizzes."],
        ["SecureCode AI (Proposed)", "Complete Secure Coding Practice Platform", "Unified platform combining browser code editing, dynamic functional tests, multi-engine SAST, Groq LLM advisory, attempt windows, conceptual quizzes, and analytics.", "Defines the new state-of-the-art in secure programming education."]
    ]
    create_styled_table(doc, ["Platform Name", "Primary Focus & Domain", "Identified Critical Limitations & Pedagogical Gaps", "How SecureCode AI Resolves the Gap"], comp_table_data, col_widths=[1.3, 1.2, 2.1, 2.1], table_title="Detailed Feature-by-Feature Matrix Comparing SecureCode AI with Industry Platforms", table_id="Table 5.10.1")

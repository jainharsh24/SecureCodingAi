from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from report_builder.styles import (
    add_chapter_heading, add_sec_heading, add_subsec_heading,
    add_p, add_bullet, NAVY_BLUE, DARK_GRAY
)

def generate_chapter6(doc):
    add_chapter_heading(doc, 6, "Conclusion and Future Scope")

    # 6.1 Conclusion
    add_sec_heading(doc, "6.1", "Conclusion")

    con_p1 = (
        "In this major project, we engineered, implemented, and empirically validated SecureCode AI—an innovative, "
        "browser-based secure coding practice and automated assessment platform designed to cultivate defensive software "
        "engineering competencies from the ground up. By bridging the critical pedagogical gap between algorithmic competitive "
        "programming (which evaluates only functional correctness) and offensive cybersecurity training (which focuses solely on "
        "vulnerability exploitation), SecureCode AI establishes a transformative paradigm in software engineering education."
    )
    add_p(doc, con_p1, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    con_p2 = (
        "The primary conclusions and technical contributions of this project are summarized below:"
    )
    add_p(doc, con_p2, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    add_bullet(doc, "The staged pipeline—evaluating dynamic functional unit tests inside an isolated Judge0 sandbox before invoking static security analyzers—optimizes server throughput, prevents computational waste on broken code, and guarantees that only functionally viable solutions undergo security analysis.", bold_prefix="1. Efficacy of Dual-Stage Evaluation: ")
    add_bullet(doc, "By orchestrating Semgrep (AST syntax matching), SpotBugs with FindSecBugs (bytecode dataflow tracking), and Groq LLaMA-3 (contextual semantic advisory) within a fail-closed consensus framework, SecureCode AI eliminates the 20%+ false-negative rate inherent in single-scanner tools, achieving zero false passes on vulnerable code.", bold_prefix="2. Multi-Engine Security Consensus: ")
    add_bullet(doc, "The development of 45 staged injection challenges spanning SQL, NoSQL, OS Command, LDAP, XPath, Header/CRLF, and Code Injection addresses the critical industry reality that while basic SQL injection is widely understood, diverse injection subtypes remain persistent threats in enterprise systems.", bold_prefix="3. Comprehensive Injection Curriculum: ")
    add_bullet(doc, "The four-pillar formula (Functional 25%, Security 45%, Understanding 15%, Independence 15%) successfully assesses multi-dimensional competence. Combined with 5-attempt rate-limiting windows and tiered hint penalties, the platform deters brute-force guessing and rewards deliberate defensive reasoning.", bold_prefix="4. Multi-Pillar Pedagogical Scoring: ")
    add_bullet(doc, "The platform was validated across 80 automated unit, integration, and security test suites, demonstrating sub-second median latency for local analysis, robust database ACID compliance across 21 Flyway migrations, and seamless responsive client rendering.", bold_prefix="5. Empirical Robustness and Performance: ")

    # 6.2 Future Scope
    add_sec_heading(doc, "6.2", "Future Scope")

    fut_p = (
        "While SecureCode AI provides a robust foundation for secure coding education, several promising avenues for future "
        "expansion and research will be pursued in Semester VIII and beyond:"
    )
    add_p(doc, fut_p, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    add_bullet(doc, "Extend the challenge curriculum beyond Java to encompass memory safety vulnerabilities in C, C++, and Rust (addressing CWE-119 Buffer Overflows, CWE-416 Use-After-Free, and Integer Overflows), utilizing LLVM sanitizers (AddressSanitizer, MemorySanitizer) within the dynamic sandbox.", bold_prefix="1. Multi-Language and Memory Safety Expansion: ")
    add_bullet(doc, "Broaden the active vulnerability catalog to cover the entire OWASP Top 10 and CWE Top 25, including Cross-Site Scripting (XSS / CWE-79), Insecure Deserialization (CWE-502), Broken Object Level Authorization (BOLA/IDOR / CWE-639), and Server-Side Request Forgery (SSRF / CWE-918).", bold_prefix="2. Full OWASP Top 10 Curriculum Coverage: ")
    add_bullet(doc, "Implement autonomous AI agents capable of dynamically analyzing submitted student code and synthesizing novel, adversarial edge-case inputs specifically tailored to expose boundary condition flaws and subtle logical bypasses.", bold_prefix="3. Autonomous Adversarial Test Generation: ")
    add_bullet(doc, "Introduce real-time defensive 'Code Battles' and collaborative team-based CTF competitions where learners race to patch vulnerable microservices under active automated attack simulations, fostering collaborative problem-solving.", bold_prefix="4. Competitive Gamification & CTF Tournaments: ")
    add_bullet(doc, "Conduct formal human-subject classroom studies across undergraduate engineering cohorts to quantitatively measure knowledge retention, pre- and post-test competency gains, and real-world defect reduction during industry internships.", bold_prefix="5. Longitudinal Pedagogical Efficacy Studies: ")

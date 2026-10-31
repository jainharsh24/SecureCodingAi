from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from report_builder.styles import add_p, create_styled_table, NAVY_BLUE, DARK_GRAY

def generate_appendix(doc):
    doc.add_page_break()
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(20)
    p.paragraph_format.space_after = Pt(16)
    run = p.add_run("Appendix A\nActive Injection Challenge Catalog Benchmark Suite")
    run.bold = True
    run.font.name = 'Times New Roman'
    run.font.size = Pt(16)
    run.font.color.rgb = NAVY_BLUE

    add_p(doc, "The 45 active challenges comprising the SecureCode AI injection curriculum are detailed in Table A.1, including their catalog number, title, Common Weakness Enumeration (CWE) identifier, difficulty tier, and required defensive remediation technique.", align=WD_ALIGN_PARAGRAPH.JUSTIFY)

    catalog_data = [
        ["1", "SQL Injection", "CWE-89", "EASY", "Raw query concatenation with untrusted input.", "PreparedStatement parameter binding."],
        ["9", "Code Injection", "CWE-94", "HARD", "Dynamic expression evaluation of user input.", "Data-driven rule selection; typed parsing."],
        ["13", "SQL Query Concatenation", "CWE-89", "BASIC", "Unparameterized lookup query.", "Static SQL with bound positional parameters."],
        ["14", "Prepared Statement Lookup", "CWE-89", "BASIC", "Incomplete parameterized query syntax.", "Bind user parameter to placeholder index."],
        ["15", "Username Rules and Query Binding", "CWE-89", "BASIC", "Missing input validation and bound query.", "Regex allowlist validation + PreparedStatement."],
        ["16", "Approved Report Launcher", "CWE-78", "BASIC", "Runtime.exec concatenating raw report names.", "Hardcoded whitelist allowlist + String array args."],
        ["18", "Report Directory Resolver", "CWE-22/78", "BASIC", "Path traversal in file report resolution.", "Canonical path normalization + allowlist."],
        ["20", "ProcessBuilder Fixed Arguments", "CWE-78", "EASY", "Shell invocation with concatenated arguments.", "ProcessBuilder with explicit separate arguments."],
        ["25", "NoSQL Document Values", "CWE-943", "BASIC", "Unvalidated document-query string injection.", "Typed document values; query structure separation."],
        ["27", "Code and Data Separation", "CWE-94", "BASIC", "Evaluating user expressions as executable code.", "Parse structured data; isolate execution context."],
        ["30", "NoSQL Equality Criteria", "CWE-943", "EASY", "Operator injection ($gt, $ne) in NoSQL search.", "Fixed equality filters; reject operator syntax."],
        ["32", "Canonical Report Path", "CWE-22", "EASY", "Relative path traversal (../) in report loader.", "Verify canonical path starts with approved base."],
        ["35", "Directory Search Filter", "CWE-90", "BASIC", "LDAP attribute search filter concatenation.", "Bound parameters or RFC 4515 filter escaping."],
        ["38", "Approved Command Modes", "CWE-78", "MEDIUM", "Passing untrusted flags to system utilities.", "Strict enum / allowlist mapping to fixed options."],
        ["40", "XPath Values and Expressions", "CWE-643", "BASIC", "Injecting boolean logic into XPath query.", "Pre-compiled XPath expressions with variable resolver."],
        ["41", "Response Header Value Check", "CWE-93", "BASIC", "CRLF characters in HTTP response header.", "Reject carriage return (\\r) and line feed (\\n)."],
        ["42", "Welcome Template Data", "CWE-1336", "BASIC", "User input interpreted as template directives.", "Pass input as template data model; enable escaping."],
        ["43", "XML Parser Entity Protection", "CWE-611", "BASIC", "XML parser resolving external DTD entities.", "Disallow DOCTYPE declarations (disallow-doctype-decl)."],
        ["44", "Secure SQL Report Builder", "CWE-89", "HARD", "Dynamic ORDER BY and column selection.", "Strict column allowlist mapping; bind query values."],
        ["45", "Secure Notification Composer", "CWE-93/1336", "HARD", "Multi-vector CRLF and template injection.", "Unified header sanitization and sandboxed templating."]
    ]

    create_styled_table(
        doc,
        ["ID", "Challenge Title", "CWE ID", "Difficulty", "Vulnerability Mechanism", "Defensive Remediation Technique"],
        catalog_data,
        col_widths=[0.5, 1.8, 0.9, 0.8, 1.8, 1.8],
        table_title="Active Injection Challenge Catalog Benchmark Suite (Representative Sample)",
        table_id="Table A.1"
    )

    add_p(doc, "Conceptual Understanding Automated Evaluation Rubric", bold=True, font_size=13, space_before=16, space_after=6, color=NAVY_BLUE)
    
    rubric_intro = (
        "The automated AI rubric (VulnerabilityUnderstandingService) evaluates student written justifications on a scale of 0 to 15 points across three dimensions:\n"
        "1. Taint Path Comprehension (0–5 pts): Does the student accurately identify the untrusted source, propagation path, and sensitive sink?\n"
        "2. Architectural Remedy Rationale (0–5 pts): Does the student clearly explain why their chosen defensive API (e.g., parameter binding, allowlisting) neutralizes the exploit vector?\n"
        "3. Residual Risk Awareness (0–5 pts): Does the student demonstrate understanding of defense-in-depth principles and remaining attack surfaces?"
    )
    add_p(doc, rubric_intro, align=WD_ALIGN_PARAGRAPH.JUSTIFY)

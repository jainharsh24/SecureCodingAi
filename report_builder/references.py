import json
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from report_builder.styles import add_p, NAVY_BLUE, DARK_GRAY

def generate_references(doc):
    doc.add_page_break()
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(20)
    p.paragraph_format.space_after = Pt(16)
    run = p.add_run("References")
    run.bold = True
    run.font.name = 'Times New Roman'
    run.font.size = Pt(16)
    run.font.color.rgb = NAVY_BLUE

    # Load 27 papers from extracted_papers.json
    with open('extracted_papers.json', 'r', encoding='utf-8') as f:
        papers = json.load(f)

    for p_item in papers:
        sno = p_item['sno']
        cit = p_item['citation']
        if not cit or cit == "None":
            cit = f"{p_item['authors']}. \"{p_item['title']}.\" {p_item['journal']}, {p_item['year']}."
        
        ref_text = f"[{sno}]  {cit}"
        p_ref = doc.add_paragraph()
        p_ref.paragraph_format.left_indent = Inches(0.4)
        p_ref.paragraph_format.first_line_indent = Inches(-0.4)
        p_ref.paragraph_format.space_before = Pt(2)
        p_ref.paragraph_format.space_after = Pt(6)
        p_ref.paragraph_format.line_spacing = 1.15
        
        run_ref = p_ref.add_run(ref_text)
        run_ref.font.name = 'Times New Roman'
        run_ref.font.size = Pt(10)
        run_ref.font.color.rgb = DARK_GRAY

    # Additional standard foundational references
    std_refs = [
        "[28]  OWASP Foundation, “OWASP Top 10: The Ten Most Critical Web Application Security Risks,” Open Web Application Security Project, 2021. [Online]. Available: https://owasp.org/www-project-top-ten/",
        "[29]  MITRE Corporation, “Common Weakness Enumeration (CWE) List,” Version 4.14, 2024. [Online]. Available: https://cwe.mitre.org/",
        "[30]  Oracle Corporation, “The CERT Oracle Secure Coding Standard for Java,” Software Engineering Institute, Carnegie Mellon University, Addison-Wesley, 2012.",
        "[31]  National Institute of Standards and Technology (NIST), “Secure Software Development Framework (SSDF) Version 1.1: Recommendations for Mitigating the Risk of Software Vulnerabilities,” NIST Special Publication 800-218, 2022."
    ]
    for sr in std_refs:
        p_ref = doc.add_paragraph()
        p_ref.paragraph_format.left_indent = Inches(0.4)
        p_ref.paragraph_format.first_line_indent = Inches(-0.4)
        p_ref.paragraph_format.space_before = Pt(2)
        p_ref.paragraph_format.space_after = Pt(6)
        p_ref.paragraph_format.line_spacing = 1.15
        
        run_ref = p_ref.add_run(sr)
        run_ref.font.name = 'Times New Roman'
        run_ref.font.size = Pt(10)
        run_ref.font.color.rgb = DARK_GRAY

import os
import shutil
import time
from report_builder.styles import init_document
from report_builder.frontmatter import generate_frontmatter
from report_builder.chapter1 import generate_chapter1
from report_builder.chapter2 import generate_chapter2
from report_builder.chapter3 import generate_chapter3
from report_builder.chapter4 import generate_chapter4
from report_builder.chapter5 import generate_chapter5
from report_builder.chapter6 import generate_chapter6
from report_builder.references import generate_references
from report_builder.appendix import generate_appendix

def main():
    print("=" * 60)
    print("Building SecureCode AI Major Project Final Report...")
    print("=" * 60)
    start_time = time.time()

    doc = init_document()

    print("[1/9] Generating Front Matter (Title, Cert, Decl, Ack, TOC, Abstract, Lists)...")
    generate_frontmatter(doc)

    print("[2/9] Generating Chapter 1: Introduction...")
    generate_chapter1(doc)

    print("[3/9] Generating Chapter 2: Problem Statement & Requirements...")
    generate_chapter2(doc)

    print("[4/9] Generating Chapter 3: Literature Review (All 27 Papers)...")
    generate_chapter3(doc)

    print("[5/9] Generating Chapter 4: Design and Implementation...")
    generate_chapter4(doc)

    print("[6/9] Generating Chapter 5: Results and Discussions (All Figures & UI)...")
    generate_chapter5(doc)

    print("[7/9] Generating Chapter 6: Conclusion and Future Scope...")
    generate_chapter6(doc)

    print("[8/9] Generating References (Consolidated IEEE Bibliography)...")
    generate_references(doc)

    print("[9/9] Generating Appendix A (Challenge Catalog & Rubric)...")
    generate_appendix(doc)

    out_file_root = "Report.docx"
    out_file_images = os.path.join("images_&_ppt", "Report.docx")

    print(f"Saving generated report to {out_file_root}...")
    doc.save(out_file_root)

    print(f"Copying report to {out_file_images}...")
    shutil.copyfile(out_file_root, out_file_images)

    root_size = os.path.getsize(out_file_root)
    elapsed = time.time() - start_time

    print("=" * 60)
    print(f"SUCCESS: Report generated successfully in {elapsed:.2f} seconds!")
    print(f"Total Paragraphs: {len(doc.paragraphs)}")
    print(f"Total Tables: {len(doc.tables)}")
    print(f"File Size: {root_size / 1024 / 1024:.2f} MB ({root_size:,} bytes)")
    print(f"Location 1: {os.path.abspath(out_file_root)}")
    print(f"Location 2: {os.path.abspath(out_file_images)}")
    print("=" * 60)

if __name__ == "__main__":
    main()

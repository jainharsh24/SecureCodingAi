import pypdf

reader = pypdf.PdfReader('images_&_ppt/Major_Project_sample_Report.pdf')
print('Total pages:', len(reader.pages))

# Print pages 6-10 (TOC, List of Figures, List of Tables)
with open('sample_pdf_frontmatter.txt', 'w', encoding='utf-8') as f:
    for i in range(len(reader.pages)):
        txt = reader.pages[i].extract_text()
        f.write(f"\n--- PAGE {i+1} ---\n")
        f.write(txt)

print("Saved full text to sample_pdf_frontmatter.txt")

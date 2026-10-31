import pypdf
reader = pypdf.PdfReader('images_&_ppt/researchpaper_sample.pdf')
print('=== RESEARCH PAPER HEADINGS ===')
for idx, page in enumerate(reader.pages):
    txt = page.extract_text()
    for line in txt.split('\n'):
        line_s = line.strip()
        if any(line_s.startswith(f'{i}.') or line_s.startswith(f'{i} ') for i in range(1, 15)):
            print(f'P{idx+1}: {line_s}')
        elif line_s in ['Abstract', 'References', 'Keywords']:
            print(f'P{idx+1}: {line_s}')

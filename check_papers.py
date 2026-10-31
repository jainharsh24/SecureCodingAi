import json

with open('extracted_papers.json', 'r', encoding='utf-8') as f:
    papers = json.load(f)

for p in papers:
    print(f"[{p['sno']}] {p['authors']}, \"{p['title']}\", {p['journal']}, {p['year']}.")
    print(f"     Methodology: {p['methodology'][:100] if p['methodology'] else 'None'}...")
    print(f"     Relevance: {p['takeaways'][:100] if p['takeaways'] else 'None'}...")
    print(f"     Citation: {p['citation']}")
    print()

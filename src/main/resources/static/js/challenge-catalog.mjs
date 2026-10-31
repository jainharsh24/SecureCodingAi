export const studentCatalogNumber = challenge => challenge.catalogNumber ?? challenge.challengeNumber;

const difficultyRank = {
  BASIC: 0,
  EASY: 1,
  MEDIUM: 2,
  HARD: 3
};

export function filterAndSortChallenges(challenges, { query = "", difficulty = "", sort = "title" } = {}) {
  const normalizedQuery = String(query).trim().toLowerCase();
  const numericCatalogQuery = /^\d+$/u.test(normalizedQuery) ? Number(normalizedQuery) : null;
  const result = challenges.filter(challenge => {
    const matchesQuery = numericCatalogQuery !== null ? studentCatalogNumber(challenge) === numericCatalogQuery
      : !normalizedQuery || [challenge.title, challenge.description,
      challenge.vulnerabilityType, challenge.vulnerabilitySubtype]
      .some(value => String(value ?? "").toLowerCase().includes(normalizedQuery));
    return matchesQuery && (!difficulty || challenge.difficulty === difficulty);
  });
  return result.toSorted((left, right) => {
    if (sort === "number-asc") return studentCatalogNumber(left) - studentCatalogNumber(right);
    if (sort === "number-desc") return studentCatalogNumber(right) - studentCatalogNumber(left);
    if (sort === "difficulty") return (difficultyRank[left.difficulty] ?? Number.MAX_SAFE_INTEGER) - (difficultyRank[right.difficulty] ?? Number.MAX_SAFE_INTEGER)
      || left.title.localeCompare(right.title);
    if (sort === "attempts") return left.attemptsRemaining - right.attemptsRemaining || left.title.localeCompare(right.title);
    return left.title.localeCompare(right.title);
  });
}

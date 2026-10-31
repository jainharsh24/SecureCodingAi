import assert from "node:assert/strict";
import test from "node:test";
import { filterAndSortChallenges, studentCatalogNumber } from "../../main/resources/static/js/challenge-catalog.mjs";

const catalog = Array.from({ length: 45 }, (_, index) => ({
  catalogNumber: index + 1, challengeNumber: index + 13, title: `Challenge ${index + 1}`,
  description: "Practice", difficulty: index < 16 ? "BASIC" : "EASY", attemptsRemaining: 5
}));

test("searches by student-facing catalog number rather than stored challenge number", () => {
  for (const number of [1, 24, 25, 26, 45]) {
    assert.deepEqual(filterAndSortChallenges(catalog, { query: ` ${number} ` }).map(studentCatalogNumber), [number]);
  }
  assert.deepEqual(filterAndSortChallenges(catalog, { query: "Challenge 24" }).map(studentCatalogNumber), [24]);
  assert.equal(filterAndSortChallenges(catalog, { query: "   " }).length, 45);
});

test("number sorting composes with difficulty filtering", () => {
  assert.deepEqual(filterAndSortChallenges(catalog, { difficulty: "BASIC", sort: "number-desc" }).map(studentCatalogNumber),
    Array.from({ length: 16 }, (_, index) => 16 - index));
});

test("difficulty sorting follows the learning progression and preserves filters", () => {
  const challenges = [
    { ...catalog[0], title: "Hard first alphabetically", difficulty: "HARD" },
    { ...catalog[1], title: "Medium", difficulty: "MEDIUM" },
    { ...catalog[2], title: "Basic", difficulty: "BASIC" },
    { ...catalog[3], title: "Easy", difficulty: "EASY" },
    { ...catalog[4], title: "Hard second alphabetically", difficulty: "HARD" }
  ];

  assert.deepEqual(filterAndSortChallenges(challenges, { sort: "difficulty" }).map(challenge => challenge.difficulty),
    ["BASIC", "EASY", "MEDIUM", "HARD", "HARD"]);
  assert.deepEqual(filterAndSortChallenges(challenges, { difficulty: "HARD", sort: "difficulty" }).map(challenge => challenge.title),
    ["Hard first alphabetically", "Hard second alphabetically"]);
});

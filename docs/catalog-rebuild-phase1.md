# Catalog rebuild — Phase 1 research and progression

Scope: SQL, NoSQL, command, LDAP, XPath, and XML external entity (XXE) injection only. The existing active catalog already provides the necessary staged learning objectives, test cases, and prerequisite edges; this review intentionally adds no duplicate challenges or relationships.

| Subtype | Existing progression | Defensive technique |
| --- | --- | --- |
| SQL Injection | SQL Query Concatenation (BASIC) → Prepared Statement Lookup (BASIC) → SQL Injection (EASY) → SQL Search with Two Parameters (EASY) → SQL Sort-Field Allowlist (MEDIUM) → Secure SQL Report Builder (HARD) | Fixed SQL with `PreparedStatement` parameters; allowlist dynamic identifiers. |
| NoSQL Injection | NoSQL Document Values (BASIC) → NoSQL Equality Criteria (EASY) → NoSQL Injection (MEDIUM) → NoSQL Operator Injection (MEDIUM) | Typed/equality criteria, fixed query structure, and allowlisted values; reject operators and document syntax. |
| Command Injection | Approved Report Launcher (BASIC) → ProcessBuilder Fixed Arguments (EASY) → Command Injection (MEDIUM) → Approved Command Modes (MEDIUM) | Fixed executable and separate `ProcessBuilder` arguments plus allowlisted command/mode values. |
| LDAP Injection | Directory Search Filter (BASIC) → LDAP Special-Character Escaping (EASY) → LDAP Injection (MEDIUM) | Fixed filter with parameter/binding or context-correct filter escaping; validation is defense in depth. |
| XPath Injection | XPath Values and Expressions (BASIC) → XPath Variable Binding (EASY) → XPath Injection (MEDIUM) | Keep the expression fixed; bind values or compare authorized records in application code. |
| XXE Injection | XML Parser Entity Protection (BASIC) → XXE Injection (HARD) → Secure XML Parser Configuration (MEDIUM) → Secure XML Import Pipeline (HARD) | Reject DTD/DOCTYPE and disable external entity and external-DTD resolution; treat failed hardening as failure. |

Existing V11 prerequisite edges connect each foundation to its downstream remediation: SQL Injection requires SQL Query Concatenation and Prepared Statement Lookup; NoSQL Injection requires NoSQL Document Values and NoSQL Equality Criteria; Command Injection requires Approved Report Launcher and ProcessBuilder Fixed Arguments; LDAP Injection requires Directory Search Filter and LDAP Special-Character Escaping; XPath Injection requires XPath Values and Expressions and XPath Variable Binding; XXE Injection requires XML Parser Entity Protection and Secure XML Parser Configuration.

Sources consulted: [OWASP SQL Injection Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html), [OWASP NoSQL Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/NoSQL_Security_Cheat_Sheet.html), [OWASP OS Command Injection Defense Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/OS_Command_Injection_Defense_Cheat_Sheet.html), [OWASP LDAP Injection Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/LDAP_Injection_Prevention_Cheat_Sheet.html), [OWASP XPath Injection Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/XPath_Injection_Prevention_Cheat_Sheet.html), and [OWASP XXE Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html).

## Verification — 22 October 2026

- Focused Maven tests completed successfully: `CatalogPreservationIntegrationTests` (4 tests) and `PrerequisiteAccessServiceTests` (2 tests), with 0 failures and 0 errors. The catalog integration test compiles every active secure reference implementation and runs each configured functional case.
- The local PostgreSQL database reports 45 active challenges: BASIC 16, EASY 9, MEDIUM 13, HARD 7. Each of the six named injection challenges has two configured test cases.
- Flyway validated 14 migrations and reported schema version 13 during the focused test. No existing/applied migration was edited in this phase; no migration was added.
- The prerequisite table has 44 edges and a missing-reference check returned 0. The graph’s duplicate/self/cycle validation was previously completed at version 13 (0/0/0); this phase did not modify the graph.
- Historical records remain present: 7 submissions, 7 functional evaluations, and 6 security evaluations linked to original challenge numbers 1–12.
- Not tested in this phase: live Judge0 execution, live Semgrep/SpotBugs execution, or browser automation. The existing integration tests execute the reference-code cases locally, not through Judge0.

# Catalog rebuild — Phase 2 research and audit

Scope: CRLF/HTTP-header, server-side template, code, email-header, GraphQL, and dynamic SQL identifier/query-construction injection. Phase 1 was read before this review. The audit found that V7, V8, V10, and V11 already provide the requested staged material, reference implementations, configured functional/security-oriented cases, and relationships. This phase deliberately adds no duplicate challenge, relationship, or Flyway migration.

## Research and learning progression

| Subtype | Mechanism and mitigation | Beginner progression already active |
| --- | --- | --- |
| CRLF / HTTP header | CR or LF in an untrusted header value can create a second header/response. Reject both control characters before using a framework header API (CWE-93). | Response Header Value Check (BASIC) → CRLF Injection (MEDIUM) → Secure Notification Composer (HARD, shared integration exercise). |
| SSTI | Untrusted input must not become template source or select a template. Render a fixed, reviewed template and pass untrusted values as data; keep HTML output escaping on (CWE-1336). | Welcome Template Data (BASIC) → Template Output Context (MEDIUM) → Template/SSTI Injection (HARD). |
| Code injection | Evaluating user text as expressions/source lets input control the interpreter. Parse typed data and select approved rules instead of dynamically executing it (CWE-94). | Code and Data Separation (BASIC) → Data-Driven Rule Selection (MEDIUM) → Code Injection (HARD). |
| Email header | A subject/recipient containing CRLF can add or alter mail headers. Reject CR/LF and give the remaining value to the mail API as a header value (CWE-93). | Email Subject Header Check (BASIC) → Email Header Injection (MEDIUM) → Secure Notification Composer (HARD, shared header/template context). |
| GraphQL | Interpolating input into a query document can alter query syntax. Keep the query fixed, use typed variables, allowlist/validate inputs, and retain resolver authorization controls (CWE-943 in this catalog). | GraphQL Variables and ID Check (BASIC) → GraphQL Query Variables (EASY) → Typed GraphQL Variables (MEDIUM) → GraphQL Injection (HARD). |
| Dynamic SQL identifiers and query construction | Bind values with `PreparedStatement`; placeholders cannot stand for identifiers, so map a requested sort/table/column choice to a fixed allowlist before composing that limited part (CWE-89). | SQL Query Concatenation (BASIC) → Prepared Statement Lookup (BASIC) → SQL Search with Two Parameters (EASY) → SQL Sort-Field Allowlist (MEDIUM) → Secure SQL Report Builder (HARD). |

The preparation challenges use one explicit concept at a time: fixed versus data-controlled syntax, `?` value binding, numeric parsing, finite allowlists, and CR/LF rejection. The later challenges then combine those concepts against representative injection sinks. This is sufficient coverage; a separate “dynamic SQL” duplicate would overlap the existing allowlist/report-builder path.

## Sources consulted

- [OWASP HTTP Response Splitting testing guidance](https://wstg.owasp.org/latest/4-Web_Application_Security_Testing/07-Injection/15-HTTP_Response_Splitting/) and [CWE-93](https://cwe.mitre.org/data/definitions/93.html) describe CR/LF insertion into HTTP or mail headers.
- [OWASP SSTI Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Server_Side_Template_Injection_Prevention_Cheat_Sheet.html) identifies untrusted template source/selection and recommends fixed templates plus data-only variables.
- [OWASP Code Injection guidance](https://community.owasp.org/attacks/Code_Injection) and [CWE-94](https://cwe.mitre.org/data/definitions/94) distinguish interpreted injected code from command injection.
- [OWASP GraphQL Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/GraphQL_Cheat_Sheet.html) calls for typed schemas/variables, strict validation, safe downstream APIs, and authorization.
- [OWASP SQL Injection Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html) recommends parameterized queries for values and a server-side mapping/allowlist for unavoidable identifiers.

## Complete combined prerequisite mapping

The twelve subtype paths below describe every direct prerequisite into the named security target and the relevant supporting progression edges. All are existing V11 rows; none were changed in this phase.

| # | Security target | Direct prerequisites | Supporting prerequisite progression |
| --- | --- | --- | --- |
| 1 | SQL Injection | SQL Query Concatenation; Prepared Statement Lookup | SQL Search with Two Parameters requires both; SQL Sort-Field Allowlist requires SQL Search; Secure SQL Report Builder requires SQL Search and SQL Sort-Field Allowlist. |
| 2 | NoSQL Injection | NoSQL Document Values; NoSQL Equality Criteria | NoSQL Equality Criteria requires NoSQL Document Values; NoSQL Operator Injection requires NoSQL Equality Criteria. |
| 3 | Command Injection | Approved Report Launcher; ProcessBuilder Fixed Arguments | ProcessBuilder Fixed Arguments requires Approved Report Launcher; Approved Command Modes requires ProcessBuilder Fixed Arguments. |
| 4 | LDAP Injection | Directory Search Filter; LDAP Special-Character Escaping | LDAP Special-Character Escaping requires Directory Search Filter. |
| 5 | XPath Injection | XPath Values and Expressions; XPath Variable Binding | XPath Variable Binding requires XPath Values and Expressions. |
| 6 | XXE Injection | XML Parser Entity Protection; Secure XML Parser Configuration | Secure XML Parser Configuration requires XML Parser Entity Protection; Secure XML Import Pipeline requires Secure XML Parser Configuration. |
| 7 | CRLF Injection | Response Header Value Check | Secure Notification Composer also requires Response Header Value Check. |
| 8 | Template/SSTI Injection | Welcome Template Data; Template Output Context | Template Output Context requires Welcome Template Data and HTML Text Encoder; Secure Notification Composer requires Template Output Context. |
| 9 | Code Injection | Code and Data Separation; Data-Driven Rule Selection | Data-Driven Rule Selection requires Code and Data Separation. |
| 10 | Email Header Injection | Email Subject Header Check | Secure Notification Composer also requires Email Subject Header Check. |
| 11 | GraphQL Injection | GraphQL Variables and ID Check; GraphQL Query Variables; Typed GraphQL Variables | GraphQL Query Variables requires GraphQL Variables and ID Check; Typed GraphQL Variables requires GraphQL Query Variables. |
| 12 | Dynamic SQL construction | Secure SQL Report Builder is the existing capstone | The SQL supporting path in row 1 supplies its direct prerequisites and the required identifier allowlist. |

## Audit result and catalog changes

No gap requiring an additive migration was found. The secure references use the intended defensive mechanism (parameter/typed data separation, finite allowlists, or CR/LF rejection), and V8/V10 include normal and hostile configured cases. Therefore Phase 2 changed only this audit document; the catalog, existing applied migrations, difficulty badges, prerequisite enforcement, numbering, authentication, evaluators, and historical records were left untouched.

## Verification — 22 October 2026

- `mvn test` completed successfully: **21 tests, 0 failures, 0 errors, 1 skipped**. It includes `CatalogPreservationIntegrationTests`, which compiled all 45 active secure reference implementations and executed every configured case, and the functional/security-evaluation service tests. The skipped test is environment-dependent.
- `node --check` completed successfully for every `.js` and `.mjs` file in `src/main/resources/static/js` and `src/test/javascript`. `node --test src/test/javascript/challenge-catalog.test.mjs` passed **2/2** regression tests.
- Flyway validated 14 entries (the baseline plus V2–V13) and reports the local schema at **version 13**. Every migration record is successful; no migration ran during this phase and no applied migration was modified.
- Read-only database checks report **45 active challenges**: BASIC **16**, EASY **9**, MEDIUM **13**, HARD **7**. The graph has **44** edges, with **0** missing references, **0** duplicate edges, **0** self-dependencies, and **0** cycles.
- Historical data is intact: **12** original challenge records (numbers 1–12), **7** submissions, **7** functional evaluations, and **6** security evaluations associated with them.
- Not run: live Judge0 execution, live Semgrep execution through WSL, live SpotBugs/FindSecBugs execution, or browser automation. The reference-case integration runs local Java processes, not Judge0.

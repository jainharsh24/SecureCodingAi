-- Preserve every existing challenge and its submission relationships.  Only new
-- catalog rows are active; existing rows remain available through submissions.
ALTER TABLE challenges ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
CREATE INDEX idx_challenges_active_number ON challenges (is_active, challenge_no);

UPDATE challenges SET is_active = FALSE;

INSERT INTO challenges (challenge_no, title, description, language, vulnerability_type, vulnerability_subtype, cwe, starter_code, secure_reference_code, difficulty, is_active)
VALUES
(nextval('challenge_number_sequence'), 'SQL Injection', 'Look up a product by name. The starter code builds SQL from untrusted input; use a parameterized query and bind the value.', 'java', 'Injection', 'SQL Injection', 'CWE-89',
$$import java.util.Scanner;
class Main { static class Database { void query(String sql) { } } public static void main(String[] args) { String name = new Scanner(System.in).nextLine(); new Database().query("SELECT * FROM products WHERE name = '" + name + "'"); System.out.println("Product: " + name); } }$$,
$$import java.util.Scanner;
class Main { static class Database { void query(String sql, String value) { } } public static void main(String[] args) { String name = new Scanner(System.in).nextLine(); new Database().query("SELECT * FROM products WHERE name = ?", name); System.out.println("Product: " + name); } }$$, 'EASY', TRUE),
(nextval('challenge_number_sequence'), 'NoSQL Injection', 'Find a profile by username. The starter code interpolates input into a document query; call a typed lookup method instead.', 'java', 'Injection', 'NoSQL Injection', 'CWE-943',
$$import java.util.Scanner;
class Main { static class Profiles { void find(String query) { } } public static void main(String[] args) { String user = new Scanner(System.in).nextLine(); new Profiles().find("{ username: '" + user + "' }"); System.out.println("Profile: " + user); } }$$,
$$import java.util.Scanner;
class Main { static class Profiles { void findByUsername(String user) { } } public static void main(String[] args) { String user = new Scanner(System.in).nextLine(); new Profiles().findByUsername(user); System.out.println("Profile: " + user); } }$$, 'MEDIUM', TRUE),
(nextval('challenge_number_sequence'), 'Command Injection', 'Run an approved report. The starter code builds an operating-system command from input; allow only known report names.', 'java', 'Injection', 'Command Injection', 'CWE-78',
$$import java.util.Scanner;
class Main { public static void main(String[] args) throws Exception { String report = new Scanner(System.in).nextLine(); Runtime.getRuntime().exec("echo " + report); System.out.println("Report: " + report); } }$$,
$$import java.util.Scanner;
class Main { public static void main(String[] args) { String report = new Scanner(System.in).nextLine(); if (!report.equals("daily") && !report.equals("weekly")) { System.out.println("Report: invalid"); return; } System.out.println("Report: " + report); } }$$, 'MEDIUM', TRUE),
(nextval('challenge_number_sequence'), 'LDAP Injection', 'Search the directory for a user. The starter code creates an LDAP filter with user input; use a directory method that accepts the value separately.', 'java', 'Injection', 'LDAP Injection', 'CWE-90',
$$import java.util.Scanner;
class Main { static class Directory { void search(String filter) { } } public static void main(String[] args) { String uid = new Scanner(System.in).nextLine(); new Directory().search("(uid=" + uid + ")"); System.out.println("Directory user: " + uid); } }$$,
$$import java.util.Scanner;
class Main { static class Directory { void findByUid(String uid) { } } public static void main(String[] args) { String uid = new Scanner(System.in).nextLine(); new Directory().findByUid(uid); System.out.println("Directory user: " + uid); } }$$, 'MEDIUM', TRUE),
(nextval('challenge_number_sequence'), 'XPath Injection', 'Find an account owner in XML. The starter code interpolates input into an XPath expression; use a typed lookup API.', 'java', 'Injection', 'XPath Injection', 'CWE-643',
$$import java.util.Scanner;
class Main { static class Accounts { void evaluate(String expression) { } } public static void main(String[] args) { String owner = new Scanner(System.in).nextLine(); new Accounts().evaluate("/accounts/account[owner='" + owner + "']"); System.out.println("Account owner: " + owner); } }$$,
$$import java.util.Scanner;
class Main { static class Accounts { void findByOwner(String owner) { } } public static void main(String[] args) { String owner = new Scanner(System.in).nextLine(); new Accounts().findByOwner(owner); System.out.println("Account owner: " + owner); } }$$, 'MEDIUM', TRUE),
(nextval('challenge_number_sequence'), 'XML Injection', 'Store a display name in an XML response. The starter code concatenates user input into markup; use an XML writer method that creates text nodes.', 'java', 'Injection', 'XML Injection', 'CWE-91',
$$import java.util.Scanner;
class Main { static class XmlResponse { void write(String xml) { } } public static void main(String[] args) { String name = new Scanner(System.in).nextLine(); new XmlResponse().write("<user>" + name + "</user>"); System.out.println("XML user: " + name); } }$$,
$$import java.util.Scanner;
class Main { static class XmlResponse { void writeUserText(String name) { } } public static void main(String[] args) { String name = new Scanner(System.in).nextLine(); new XmlResponse().writeUserText(name); System.out.println("XML user: " + name); } }$$, 'EASY', TRUE),
(nextval('challenge_number_sequence'), 'CRLF Injection', 'Set a tracking response header. The starter code writes user input into a header; reject carriage returns and line feeds before setting it.', 'java', 'Injection', 'CRLF Injection', 'CWE-93',
$$import java.util.Scanner;
class Main { static class Response { void header(String value) { } } public static void main(String[] args) { String value = new Scanner(System.in).nextLine(); new Response().header("X-Tracking: " + value); System.out.println("Tracking: " + value); } }$$,
$$import java.util.Scanner;
class Main { static class Response { void trackingHeader(String value) { } } public static void main(String[] args) { String value = new Scanner(System.in).nextLine(); if (value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) { System.out.println("Tracking: invalid"); return; } new Response().trackingHeader(value); System.out.println("Tracking: " + value); } }$$, 'MEDIUM', TRUE),
(nextval('challenge_number_sequence'), 'Template/SSTI Injection', 'Render a welcome message. The starter code treats input as part of a template; pass it as a model value to a fixed template.', 'java', 'Injection', 'Template/SSTI Injection', 'CWE-1336',
$$import java.util.Scanner;
class Main { static class Templates { void render(String template) { } } public static void main(String[] args) { String name = new Scanner(System.in).nextLine(); new Templates().render("Welcome " + name); System.out.println("Welcome: " + name); } }$$,
$$import java.util.Scanner;
class Main { static class Templates { void renderWelcome(String name) { } } public static void main(String[] args) { String name = new Scanner(System.in).nextLine(); new Templates().renderWelcome(name); System.out.println("Welcome: " + name); } }$$, 'HARD', TRUE),
(nextval('challenge_number_sequence'), 'Code Injection', 'Evaluate an approved numeric level. The starter code turns user input into source code; parse and validate a number instead.', 'java', 'Injection', 'Code Injection', 'CWE-94',
$$import java.util.Scanner;
class Main { static class ScriptEngine { void execute(String source) { } } public static void main(String[] args) { String level = new Scanner(System.in).nextLine(); new ScriptEngine().execute("return " + level); System.out.println("Level: " + level); } }$$,
$$import java.util.Scanner;
class Main { public static void main(String[] args) { String text = new Scanner(System.in).nextLine(); try { int level = Integer.parseInt(text); if (level < 1 || level > 5) throw new NumberFormatException(); System.out.println("Level: " + level); } catch (NumberFormatException exception) { System.out.println("Level: invalid"); } } }$$, 'HARD', TRUE),
(nextval('challenge_number_sequence'), 'Email Header Injection', 'Create an email subject from a ticket title. The starter code builds a raw header; reject control characters and pass the subject to the mail API.', 'java', 'Injection', 'Email Header Injection', 'CWE-93',
$$import java.util.Scanner;
class Main { static class Mail { void raw(String message) { } } public static void main(String[] args) { String subject = new Scanner(System.in).nextLine(); new Mail().raw("Subject: " + subject); System.out.println("Subject: " + subject); } }$$,
$$import java.util.Scanner;
class Main { static class Mail { void subject(String value) { } } public static void main(String[] args) { String value = new Scanner(System.in).nextLine(); if (value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) { System.out.println("Subject: invalid"); return; } new Mail().subject(value); System.out.println("Subject: " + value); } }$$, 'MEDIUM', TRUE),
(nextval('challenge_number_sequence'), 'XXE Injection', 'Process an uploaded XML profile. The starter code permits external entities; use a parser configuration that disables them.', 'java', 'Injection', 'XXE Injection', 'CWE-611',
$$import java.util.Scanner;
class Main { static class XmlParser { void parseWithExternalEntities(String xml) { } } public static void main(String[] args) { String xml = new Scanner(System.in).nextLine(); new XmlParser().parseWithExternalEntities(xml); System.out.println("XML: accepted"); } }$$,
$$import java.util.Scanner;
class Main { static class XmlParser { void parseWithoutExternalEntities(String xml) { } } public static void main(String[] args) { String xml = new Scanner(System.in).nextLine(); new XmlParser().parseWithoutExternalEntities(xml); System.out.println("XML: accepted"); } }$$, 'HARD', TRUE),
(nextval('challenge_number_sequence'), 'GraphQL Injection', 'Fetch a customer by ID. The starter code interpolates input into a GraphQL query; use a fixed query with a variables map.', 'java', 'Injection', 'GraphQL Injection', 'CWE-943',
$$import java.util.Scanner;
class Main { static class GraphQl { void execute(String query) { } } public static void main(String[] args) { String id = new Scanner(System.in).nextLine(); new GraphQl().execute("{ customer(id: \\\"" + id + "\\\") { name } }"); System.out.println("Customer: " + id); } }$$,
$$import java.util.Scanner;
class Main { static class GraphQl { void execute(String query, String id) { } } public static void main(String[] args) { String id = new Scanner(System.in).nextLine(); new GraphQl().execute("query($id: ID!) { customer(id: $id) { name } }", id); System.out.println("Customer: " + id); } }$$, 'HARD', TRUE);

INSERT INTO test_cases (challenge_id, input_data, expected_output, is_hidden, position)
SELECT challenges.id, cases.input_data, cases.expected_output, cases.is_hidden, cases.position
FROM (VALUES
  ('SQL Injection', 'widget', 'Product: widget', FALSE, 0), ('SQL Injection', 'blue pen', 'Product: blue pen', TRUE, 1),
  ('NoSQL Injection', 'alex', 'Profile: alex', FALSE, 0), ('NoSQL Injection', 'sam', 'Profile: sam', TRUE, 1),
  ('Command Injection', 'daily', 'Report: daily', FALSE, 0), ('Command Injection', 'weekly', 'Report: weekly', TRUE, 1),
  ('LDAP Injection', 'mira', 'Directory user: mira', FALSE, 0), ('LDAP Injection', 'dev01', 'Directory user: dev01', TRUE, 1),
  ('XPath Injection', 'ravi', 'Account owner: ravi', FALSE, 0), ('XPath Injection', 'lee', 'Account owner: lee', TRUE, 1),
  ('XML Injection', 'Jordan', 'XML user: Jordan', FALSE, 0), ('XML Injection', 'Taylor', 'XML user: Taylor', TRUE, 1),
  ('CRLF Injection', 'session-42', 'Tracking: session-42', FALSE, 0), ('CRLF Injection', 'audit-9', 'Tracking: audit-9', TRUE, 1),
  ('Template/SSTI Injection', 'Asha', 'Welcome: Asha', FALSE, 0), ('Template/SSTI Injection', 'Noah', 'Welcome: Noah', TRUE, 1),
  ('Code Injection', '3', 'Level: 3', FALSE, 0), ('Code Injection', '5', 'Level: 5', TRUE, 1),
  ('Email Header Injection', 'Ticket update', 'Subject: Ticket update', FALSE, 0), ('Email Header Injection', 'Build notice', 'Subject: Build notice', TRUE, 1),
  ('XXE Injection', '<profile/>', 'XML: accepted', FALSE, 0), ('XXE Injection', '<user/>', 'XML: accepted', TRUE, 1),
  ('GraphQL Injection', '42', 'Customer: 42', FALSE, 0), ('GraphQL Injection', '7', 'Customer: 7', TRUE, 1)
) AS cases(title, input_data, expected_output, is_hidden, position)
JOIN challenges ON challenges.title = cases.title AND challenges.is_active = TRUE;

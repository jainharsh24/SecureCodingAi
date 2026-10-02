CREATE SEQUENCE challenge_number_sequence START WITH 1;

ALTER TABLE challenges ADD COLUMN challenge_no BIGINT;

WITH numbered AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS number
    FROM challenges
)
UPDATE challenges SET challenge_no = numbered.number FROM numbered WHERE challenges.id = numbered.id;

ALTER TABLE challenges ALTER COLUMN challenge_no SET NOT NULL;
ALTER TABLE challenges ADD CONSTRAINT uk_challenges_challenge_no UNIQUE (challenge_no);
SELECT setval('challenge_number_sequence', COALESCE((SELECT MAX(challenge_no) FROM challenges), 0) + 1, false);

ALTER TABLE challenges ADD COLUMN secure_reference_code TEXT;

INSERT INTO challenges (challenge_no, title, description, language, vulnerability_type, vulnerability_subtype, cwe, starter_code, secure_reference_code, difficulty)
VALUES
(nextval('challenge_number_sequence'), 'Customer Lookup Query', 'Read a customer name and look it up. The query currently joins untrusted input directly into a SQL query; use a parameterized query instead.', 'java', 'Injection Attack', 'SQL query injection', 'CWE-89',
$$import java.util.Scanner;
class Main {
  static class Database { void executeQuery(String query) { } }
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String customer = scanner.nextLine();
    String query = "SELECT * FROM customers WHERE name = '" + customer + "'";
    new Database().executeQuery(query);
    System.out.println("Lookup: " + customer);
  }
}$$,
$$import java.util.Scanner;
class Main {
  static class Database { void executeQuery(String query) { } void bind(String value) { } }
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in); String customer = scanner.nextLine();
    Database database = new Database(); database.executeQuery("SELECT * FROM customers WHERE name = ?"); database.bind(customer);
    System.out.println("Lookup: " + customer);
  }
}$$, 'EASY'),
(nextval('challenge_number_sequence'), 'Order Status Update', 'Read an order status and update the order record. Do not build the UPDATE statement with user-controlled text; use parameter binding.', 'java', 'Injection Attack', 'SQL update injection', 'CWE-89',
$$import java.util.Scanner;
class Main {
  static class Database { void executeUpdate(String query) { } }
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in); String status = scanner.nextLine();
    String query = "UPDATE orders SET status = '" + status + "' WHERE id = 7";
    new Database().executeUpdate(query); System.out.println("Status: " + status);
  }
}$$,
$$import java.util.Scanner;
class Main {
  static class Database { void executeUpdate(String query) { } void bind(String value) { } }
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in); String status = scanner.nextLine(); Database database = new Database();
    database.executeUpdate("UPDATE orders SET status = ? WHERE id = 7"); database.bind(status); System.out.println("Status: " + status);
  }
}$$, 'EASY'),
(nextval('challenge_number_sequence'), 'Report Command Runner', 'Read a report name and run the report command. Avoid passing a user-built command to the operating system; accept only known report names.', 'java', 'Injection Attack', 'OS command injection', 'CWE-78',
$$import java.util.Scanner;
class Main {
  public static void main(String[] args) throws Exception {
    Scanner scanner = new Scanner(System.in); String report = scanner.nextLine();
    Runtime.getRuntime().exec("echo " + report); System.out.println("Requested: " + report);
  }
}$$,
$$import java.util.Scanner;
class Main {
  public static void main(String[] args) throws Exception {
    Scanner scanner = new Scanner(System.in); String report = scanner.nextLine();
    if (!report.equals("daily") && !report.equals("weekly")) { System.out.println("Requested: invalid"); return; }
    Runtime.getRuntime().exec(new String[] {"echo", "approved-report"}); System.out.println("Requested: " + report);
  }
}$$, 'MEDIUM'),
(nextval('challenge_number_sequence'), 'Download File Selector', 'Read the name of a downloadable report. Keep file access inside the reports directory by mapping approved names to fixed paths.', 'java', 'Injection Attack', 'Path traversal', 'CWE-22',
$$import java.io.File;
import java.util.Scanner;
class Main {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in); String fileName = scanner.nextLine();
    File file = new File("reports/" + fileName); System.out.println("File: " + file.getPath());
  }
}$$,
$$import java.io.File;
import java.util.Scanner;
class Main {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in); String fileName = scanner.nextLine(); File file;
    if (fileName.equals("daily.txt")) file = new File("reports/daily.txt");
    else if (fileName.equals("weekly.txt")) file = new File("reports/weekly.txt");
    else { System.out.println("File: invalid"); return; }
    System.out.println("File: " + file.getPath());
  }
}$$, 'MEDIUM'),
(nextval('challenge_number_sequence'), 'Directory User Search', 'Read a username and search the company directory. Do not concatenate it into an LDAP filter; call a safe directory lookup method.', 'java', 'Injection Attack', 'LDAP filter injection', 'CWE-90',
$$import java.util.Scanner;
class Main {
  static class Directory { void search(String base, String filter, Object controls) { } }
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in); String username = scanner.nextLine();
    String filter = "(uid=" + username + ")"; new Directory().search("ou=people", filter, null);
    System.out.println("User: " + username);
  }
}$$,
$$import java.util.Scanner;
class Main {
  static class Directory { void findByUid(String username) { } }
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in); String username = scanner.nextLine(); new Directory().findByUid(username);
    System.out.println("User: " + username);
  }
}$$, 'MEDIUM'),
(nextval('challenge_number_sequence'), 'XML Account Search', 'Read an account name and find it in an XML document. Do not place user input in an XPath expression; use a safe lookup API instead.', 'java', 'Injection Attack', 'XPath expression injection', 'CWE-643',
$$import java.util.Scanner;
class Main {
  static class XPath { void evaluate(String expression, Object document) { } }
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in); String account = scanner.nextLine();
    String expression = "/accounts/account[name='" + account + "']"; new XPath().evaluate(expression, null);
    System.out.println("Account: " + account);
  }
}$$,
$$import java.util.Scanner;
class Main {
  static class AccountLookup { void findByName(String account) { } }
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in); String account = scanner.nextLine(); new AccountLookup().findByName(account);
    System.out.println("Account: " + account);
  }
}$$, 'MEDIUM');

INSERT INTO test_cases (challenge_id, input_data, expected_output, is_hidden, position)
SELECT id, input_data, expected_output, false, position
FROM (VALUES
  ('Customer Lookup Query', 'Alice', 'Lookup: Alice', 0),
  ('Order Status Update', 'shipped', 'Status: shipped', 0),
  ('Report Command Runner', 'daily', 'Requested: daily', 0),
  ('Download File Selector', 'daily.txt', 'File: reports/daily.txt', 0),
  ('Directory User Search', 'alice', 'User: alice', 0),
  ('XML Account Search', 'alice', 'Account: alice', 0)
) AS data(title, input_data, expected_output, position)
JOIN challenges ON challenges.title = data.title;

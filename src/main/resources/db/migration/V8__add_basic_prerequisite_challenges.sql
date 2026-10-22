-- Add BASIC without changing any existing challenge record, identifier, or submission relationship.
ALTER TABLE challenges DROP CONSTRAINT chk_challenge_difficulty;
ALTER TABLE challenges ADD CONSTRAINT chk_challenge_difficulty
    CHECK (difficulty IN ('BASIC', 'EASY', 'MEDIUM', 'HARD'));

INSERT INTO challenges (challenge_no, title, description, language, vulnerability_type, vulnerability_subtype, cwe, starter_code, secure_reference_code, difficulty, is_active)
VALUES
(nextval('challenge_number_sequence'), 'SQL Query Concatenation',
'Read a customer name and look it up. Concatenating the name into SQL changes the query itself. Learning objective: keep SQL text fixed and pass the name as a separate value. Expected output is Customer: followed by the input.', 'java', 'Injection', 'SQL Injection Basics', 'CWE-89',
$$import java.util.*; class Main { static class Db { void query(String sql){} } public static void main(String[] a){String name=new Scanner(System.in).nextLine();new Db().query("SELECT * FROM customers WHERE name='"+name+"'");System.out.println("Customer: "+name);} }$$,
$$import java.util.*; class Main { static class Db { void query(String sql,String value){} } public static void main(String[] a){String name=new Scanner(System.in).nextLine();new Db().query("SELECT * FROM customers WHERE name = ?",name);System.out.println("Customer: "+name);} }$$, 'BASIC', TRUE),
(nextval('challenge_number_sequence'), 'Prepared Statement Lookup',
'Use the provided PreparedStatement-style API to look up a product. Learning objective: put ? in the SQL and call setString before executeQuery; do not concatenate input. Print Product: followed by the input.', 'java', 'Injection', 'Parameterized SQL Queries', 'CWE-89',
$$import java.util.*; class Main { static class Connection { void execute(String sql){} } public static void main(String[] a){String product=new Scanner(System.in).nextLine();new Connection().execute("SELECT * FROM products WHERE name='"+product+"'");System.out.println("Product: "+product);} }$$,
$$import java.util.*; class Main { static class PreparedStatement { void setString(int n,String v){} void executeQuery(){} } static class Connection { PreparedStatement prepareStatement(String sql){return new PreparedStatement();} } public static void main(String[] a){String product=new Scanner(System.in).nextLine();PreparedStatement s=new Connection().prepareStatement("SELECT * FROM products WHERE name = ?");s.setString(1,product);s.executeQuery();System.out.println("Product: "+product);} }$$, 'BASIC', TRUE),
(nextval('challenge_number_sequence'), 'Username Rules and Query Binding',
'A username may contain 3 to 20 letters, digits, or underscores. Print Username: invalid for other input. For valid input, still use a parameterized query: validation is helpful, but it is not a replacement for query binding.', 'java', 'Injection', 'Username Input Validation', 'CWE-89',
$$import java.util.*; class Main { static class Db { void query(String q){} } public static void main(String[] a){String u=new Scanner(System.in).nextLine();new Db().query("SELECT * FROM users WHERE username='"+u+"'");System.out.println("Username: "+u);} }$$,
$$import java.util.*; class Main { static class Db { void query(String q,String v){} } public static void main(String[] a){String u=new Scanner(System.in).nextLine();if(!u.matches("[A-Za-z0-9_]{3,20}")){System.out.println("Username: invalid");return;}new Db().query("SELECT * FROM users WHERE username = ?",u);System.out.println("Username: "+u);} }$$, 'BASIC', TRUE),
(nextval('challenge_number_sequence'), 'Approved Report Launcher',
'Only daily and weekly are approved report names. Print Report: invalid for any other input. Learning objective: choose from known values instead of building an operating-system command from user text.', 'java', 'Injection', 'Safe Command Execution', 'CWE-78',
$$import java.util.*; class Main { public static void main(String[] a)throws Exception{String r=new Scanner(System.in).nextLine();Runtime.getRuntime().exec("echo "+r);System.out.println("Report: "+r);} }$$,
$$import java.util.*; class Main { public static void main(String[] a){String r=new Scanner(System.in).nextLine();if(!r.equals("daily")&&!r.equals("weekly")){System.out.println("Report: invalid");return;}System.out.println("Report: "+r);} }$$, 'BASIC', TRUE),
(nextval('challenge_number_sequence'), 'HTML Text Encoder',
'Render a display name as HTML text. Encode &, <, >, double quotes, and apostrophes before printing Rendered: followed by the safe text. Learning objective: data must not become HTML markup.', 'java', 'Injection', 'Safe HTML Output', 'CWE-79',
$$import java.util.*; class Main { public static void main(String[] a){String n=new Scanner(System.in).nextLine();System.out.println("Rendered: <p>"+n+"</p>");} }$$,
$$import java.util.*; class Main { static String e(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");} public static void main(String[] a){System.out.println("Rendered: "+e(new Scanner(System.in).nextLine()));} }$$, 'BASIC', TRUE),
(nextval('challenge_number_sequence'), 'Report Directory Resolver',
'Users may request only daily.txt or weekly.txt from the reports directory. Print File: invalid otherwise. Learning objective: map approved names to fixed paths so input cannot escape the allowed directory.', 'java', 'Injection', 'Safe File Path Handling', 'CWE-22',
$$import java.util.*; class Main { public static void main(String[] a){String f=new Scanner(System.in).nextLine();System.out.println("File: reports/"+f);} }$$,
$$import java.util.*; class Main { public static void main(String[] a){String f=new Scanner(System.in).nextLine();if(!f.equals("daily.txt")&&!f.equals("weekly.txt")){System.out.println("File: invalid");return;}System.out.println("File: reports/"+f);} }$$, 'BASIC', TRUE),
(nextval('challenge_number_sequence'), 'XML Parser Entity Protection',
'Accept ordinary XML but reject input containing <!DOCTYPE. Configure the provided parser with external entities disabled. Print XML: accepted or XML: invalid. Learning objective: external entities must not be enabled for untrusted XML.', 'java', 'Injection', 'Secure XML Parsing Basics', 'CWE-611',
$$import java.util.*; class Main { static class Parser { Parser(boolean entities){} void parse(String x){} } public static void main(String[] a){String x=new Scanner(System.in).nextLine();new Parser(true).parse(x);System.out.println("XML: accepted");} }$$,
$$import java.util.*; class Main { static class Parser { Parser(boolean entities){} void parse(String x){} } public static void main(String[] a){String x=new Scanner(System.in).nextLine();if(x.contains("<!DOCTYPE")){System.out.println("XML: invalid");return;}new Parser(false).parse(x);System.out.println("XML: accepted");} }$$, 'BASIC', TRUE),
(nextval('challenge_number_sequence'), 'Welcome Template Data',
'Render a name in a fixed welcome template. Print Welcome: followed by the exact input, including characters such as {{7*7}}. Learning objective: pass input as data, not as a template expression.', 'java', 'Injection', 'Safe Template Rendering', 'CWE-1336',
$$import java.util.*; class Main { static class Templates { void render(String t){} } public static void main(String[] a){String n=new Scanner(System.in).nextLine();new Templates().render("Welcome "+n);System.out.println("Welcome: "+n);} }$$,
$$import java.util.*; class Main { static class Templates { void render(String t,String n){} } public static void main(String[] a){String n=new Scanner(System.in).nextLine();new Templates().render("Welcome {{name}}",n);System.out.println("Welcome: "+n);} }$$, 'BASIC', TRUE),
(nextval('challenge_number_sequence'), 'Response Header Value Check',
'Set a tracking header value. Reject input containing carriage return or line feed by printing Tracking: invalid; otherwise print Tracking: followed by the value. Learning objective: header values cannot contain new header lines.', 'java', 'Injection', 'HTTP Header Safety', 'CWE-93',
$$import java.util.*; class Main { static class Response { void header(String v){} } public static void main(String[] a){String v=new Scanner(System.in).nextLine();new Response().header("X-Tracking: "+v);System.out.println("Tracking: "+v);} }$$,
$$import java.util.*; class Main { static class Response { void header(String v){} } public static void main(String[] a){String v=new Scanner(System.in).nextLine();if(v.indexOf('\r')>=0||v.indexOf('\n')>=0){System.out.println("Tracking: invalid");return;}new Response().header(v);System.out.println("Tracking: "+v);} }$$, 'BASIC', TRUE),
(nextval('challenge_number_sequence'), 'Email Subject Header Check',
'Create an email subject from a ticket title. Reject carriage return or line feed with Subject: invalid; otherwise print Subject: followed by the title. Learning objective: untrusted text must not create additional email headers.', 'java', 'Injection', 'Email Header Safety', 'CWE-93',
$$import java.util.*; class Main { static class Mail { void raw(String v){} } public static void main(String[] a){String s=new Scanner(System.in).nextLine();new Mail().raw("Subject: "+s);System.out.println("Subject: "+s);} }$$,
$$import java.util.*; class Main { static class Mail { void subject(String v){} } public static void main(String[] a){String s=new Scanner(System.in).nextLine();if(s.indexOf('\r')>=0||s.indexOf('\n')>=0){System.out.println("Subject: invalid");return;}new Mail().subject(s);System.out.println("Subject: "+s);} }$$, 'BASIC', TRUE),
(nextval('challenge_number_sequence'), 'Directory Search Filter',
'Find a directory user by ID. Learning objective: keep the LDAP filter fixed and give the ID to a safe lookup method instead of joining it into (uid=...). Print Directory user: followed by the input.', 'java', 'Injection', 'LDAP Filter Basics', 'CWE-90',
$$import java.util.*; class Main { static class Directory { void search(String f){} } public static void main(String[] a){String u=new Scanner(System.in).nextLine();new Directory().search("(uid="+u+")");System.out.println("Directory user: "+u);} }$$,
$$import java.util.*; class Main { static class Directory { void findByUid(String u){} } public static void main(String[] a){String u=new Scanner(System.in).nextLine();new Directory().findByUid(u);System.out.println("Directory user: "+u);} }$$, 'BASIC', TRUE),
(nextval('challenge_number_sequence'), 'GraphQL Variables and ID Check',
'A customer ID must be one or more digits. Print Customer: invalid for other input. For valid input, use a fixed GraphQL query and a variable value. Learning objective: validation helps user feedback, while variables keep input out of query syntax.', 'java', 'Injection', 'GraphQL Input Safety', 'CWE-943',
$$import java.util.*; class Main { static class Graph { void execute(String q){} } public static void main(String[] a){String id=new Scanner(System.in).nextLine();new Graph().execute("{ customer(id: \""+id+"\") { name } }");System.out.println("Customer: "+id);} }$$,
$$import java.util.*; class Main { static class Graph { void execute(String q,String id){} } public static void main(String[] a){String id=new Scanner(System.in).nextLine();if(!id.matches("[0-9]+")){System.out.println("Customer: invalid");return;}new Graph().execute("query($id: ID!) { customer(id: $id) { name } }",id);System.out.println("Customer: "+id);} }$$, 'BASIC', TRUE);

INSERT INTO test_cases (challenge_id, input_data, expected_output, is_hidden, position)
SELECT c.id, d.input_data, d.expected_output, d.is_hidden, d.position
FROM (VALUES
('SQL Query Concatenation','Alice','Customer: Alice',FALSE,0),('SQL Query Concatenation','O''Brien','Customer: O''Brien',FALSE,1),('SQL Query Concatenation',''' OR ''1''=''1','Customer: '' OR ''1''=''1',TRUE,2),
('Prepared Statement Lookup','notebook','Product: notebook',FALSE,0),('Prepared Statement Lookup','blue pen','Product: blue pen',FALSE,1),('Prepared Statement Lookup','x'' OR ''1''=''1','Product: x'' OR ''1''=''1',TRUE,2),
('Username Rules and Query Binding','alice_42','Username: alice_42',FALSE,0),('Username Rules and Query Binding','ab','Username: invalid',FALSE,1),('Username Rules and Query Binding','bad name','Username: invalid',TRUE,2),
('Approved Report Launcher','daily','Report: daily',FALSE,0),('Approved Report Launcher','weekly','Report: weekly',FALSE,1),('Approved Report Launcher','daily; whoami','Report: invalid',TRUE,2),
('HTML Text Encoder','Asha','Rendered: Asha',FALSE,0),('HTML Text Encoder','Tom & Jerry','Rendered: Tom &amp; Jerry',FALSE,1),('HTML Text Encoder','<b>hello</b>','Rendered: &lt;b&gt;hello&lt;/b&gt;',TRUE,2),
('Report Directory Resolver','daily.txt','File: reports/daily.txt',FALSE,0),('Report Directory Resolver','weekly.txt','File: reports/weekly.txt',FALSE,1),('Report Directory Resolver','../secrets.txt','File: invalid',TRUE,2),
('XML Parser Entity Protection','<profile/>','XML: accepted',FALSE,0),('XML Parser Entity Protection','<user/>','XML: accepted',FALSE,1),('XML Parser Entity Protection','<!DOCTYPE profile>','XML: invalid',TRUE,2),
('Welcome Template Data','Asha','Welcome: Asha',FALSE,0),('Welcome Template Data','{{7*7}}','Welcome: {{7*7}}',FALSE,1),('Welcome Template Data','{{admin}}','Welcome: {{admin}}',TRUE,2),
('Response Header Value Check','session-42','Tracking: session-42',FALSE,0),('Response Header Value Check','audit-9','Tracking: audit-9',FALSE,1),('Response Header Value Check','bad value','Tracking: bad value',TRUE,2),
('Email Subject Header Check','Ticket update','Subject: Ticket update',FALSE,0),('Email Subject Header Check','Build notice','Subject: Build notice',FALSE,1),('Email Subject Header Check','Release; Bcc: all','Subject: Release; Bcc: all',TRUE,2),
('Directory Search Filter','mira','Directory user: mira',FALSE,0),('Directory Search Filter','dev01','Directory user: dev01',FALSE,1),('Directory Search Filter','*)(uid=*)','Directory user: *)(uid=*)',TRUE,2),
('GraphQL Variables and ID Check','42','Customer: 42',FALSE,0),('GraphQL Variables and ID Check','007','Customer: 007',FALSE,1),('GraphQL Variables and ID Check','7) { secrets }','Customer: invalid',TRUE,2)
) AS d(title,input_data,expected_output,is_hidden,position)
JOIN challenges c ON c.title=d.title AND c.is_active=TRUE;

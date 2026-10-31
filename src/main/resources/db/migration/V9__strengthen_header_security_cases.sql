-- Strengthen header security test cases to test actual CRLF injection
UPDATE challenges
SET starter_code = $$import java.io.*; class Main { static class Response { void header(String v){} } static String input()throws Exception{String s=new String(System.in.readAllBytes());return s.endsWith("\r\n")?s.substring(0,s.length()-2):s.endsWith("\n")?s.substring(0,s.length()-1):s;} public static void main(String[] a)throws Exception{String v=input();new Response().header("X-Tracking: "+v);System.out.println("Tracking: "+v);} }$$,
    secure_reference_code = $$import java.io.*; class Main { static class Response { void header(String v){} } static String input()throws Exception{String s=new String(System.in.readAllBytes());return s.endsWith("\r\n")?s.substring(0,s.length()-2):s.endsWith("\n")?s.substring(0,s.length()-1):s;} public static void main(String[] a)throws Exception{String v=input();if(v.indexOf('\r')>=0||v.indexOf('\n')>=0){System.out.println("Tracking: invalid");return;}new Response().header(v);System.out.println("Tracking: "+v);} }$$
WHERE title = 'Response Header Value Check' AND is_active = TRUE;

UPDATE test_cases
SET input_data = E'audit\r\nSet-Cookie: injected',
    expected_output = 'Tracking: invalid'
WHERE challenge_id = (SELECT id FROM challenges WHERE title = 'Response Header Value Check' AND is_active = TRUE)
  AND position = 2;

UPDATE challenges
SET starter_code = $$import java.io.*; class Mail { void raw(String v){} } static String input()throws Exception{String s=new String(System.in.readAllBytes());return s.endsWith("\r\n")?s.substring(0,s.length()-2):s.endsWith("\n")?s.substring(0,s.length()-1):s;} public static void main(String[] a)throws Exception{String s=input();new Mail().raw("Subject: "+s);System.out.println("Subject: "+s);} }$$,
    secure_reference_code = $$import java.io.*; class Mail { void subject(String v){} } static String input()throws Exception{String s=new String(System.in.readAllBytes());return s.endsWith("\r\n")?s.substring(0,s.length()-2):s.endsWith("\n")?s.substring(0,s.length()-1):s;} public static void main(String[] a)throws Exception{String s=input();if(s.indexOf('\r')>=0||s.indexOf('\n')>=0){System.out.println("Subject: invalid");return;}new Mail().subject(s);System.out.println("Subject: "+s);} }$$
WHERE title = 'Email Subject Header Check' AND is_active = TRUE;

UPDATE test_cases
SET input_data = E'Release\r\nBcc: everyone@example.test',
    expected_output = 'Subject: invalid'
WHERE challenge_id = (SELECT id FROM challenges WHERE title = 'Email Subject Header Check' AND is_active = TRUE)
  AND position = 2;

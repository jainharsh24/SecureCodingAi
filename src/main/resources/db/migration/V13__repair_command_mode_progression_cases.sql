-- V10 stored these intended line breaks as literal backslash-n text in PostgreSQL.
-- Keep each functional case single-line so the evaluator's appended newline is sufficient.
UPDATE challenges
SET starter_code = $$import java.util.*; class Main { public static void main(String[] args) { String[] values = new Scanner(System.in).nextLine().split(",", -1); System.out.println(values.length == 2 ? "Mode: " + values[0] + "/" + values[1] : "Mode: invalid"); } }$$,
    secure_reference_code = $$import java.util.*; class Main { public static void main(String[] args) { String[] values = new Scanner(System.in).nextLine().split(",", -1); if (values.length != 2) { System.out.println("Mode: invalid"); return; } String report = values[0], mode = values[1]; System.out.println((report.equals("daily") || report.equals("weekly")) && (mode.equals("preview") || mode.equals("full")) ? "Mode: " + report + "/" + mode : "Mode: invalid"); } }$$
WHERE title = 'Approved Command Modes' AND is_active = TRUE;

UPDATE test_cases
SET input_data = CASE position
    WHEN 0 THEN 'daily,preview'
    WHEN 1 THEN 'weekly,full'
    WHEN 2 THEN 'daily;whoami,full'
END
WHERE challenge_id = (SELECT id FROM challenges WHERE title = 'Approved Command Modes' AND is_active = TRUE)
  AND position IN (0, 1, 2);

package com.securecode.ai.service;

import com.securecode.ai.dto.UnderstandingQuestionResponse;
import com.securecode.ai.entity.Challenge;
import java.util.Locale;

/** Local, auditable fallback rubric. It awards a criterion only when its evidence is stated. */
final class UnderstandingRubric {
    private UnderstandingRubric() { }
    static UnderstandingQuestionResponse question(Challenge c) {
        String t = value(c.getVulnerabilitySubtype()).toLowerCase(Locale.ROOT);
        if (t.contains("sql") && !t.contains("nosql")) return question(c, "Why are parameterized queries safer than concatenating user input into SQL?", "Explain how input can change an SQL query.", "Identify parameterized/prepared queries.", "Explain how parameters keep data separate from SQL instructions.");
        if (t.contains("nosql")) return question(c, "Why should user input not be accepted directly as a database query object?", "Explain how query operators or object shape can be controlled by input.", "Identify validation, a schema, or an allow-list.", "Explain how it restricts operators and query structure.");
        if (t.contains("command") || t.contains("shell")) return question(c, "Why is passing untrusted input to a shell command dangerous?", "Explain how input can alter shell command execution.", "Identify avoiding the shell or using an argument-list API and allow-lists.", "Explain how this prevents command interpretation/injection.");
        String target = value(c.getVulnerabilitySubtype()); if (target.isBlank()) target = value(c.getVulnerabilityType());
        return question(c, "How can untrusted input cause " + target + ", what control should be used, and why does it help?", "Identify how input reaches a sensitive operation.", "Name suitable validation, encoding, or parameterization.", "Explain how that control limits exploitation.");
    }
    private static UnderstandingQuestionResponse question(Challenge c, String q, String r, String m, String p) { return new UnderstandingQuestionResponse(c.getId(), q, r, m, p); }
    static Result assess(Challenge c, String answer) {
        String a = answer == null ? "" : answer.toLowerCase(Locale.ROOT), t = value(c.getVulnerabilitySubtype()).toLowerCase(Locale.ROOT);
        boolean sql=t.contains("sql")&&!t.contains("nosql"), nosql=t.contains("nosql"), command=t.contains("command")||t.contains("shell");
        boolean root=sql?(has(a,"concatenat","inject")||(a.contains("input")&&a.contains("query"))):nosql?(has(a,"$ne","$gt","inject","query object")||(a.contains("operator")&&a.contains("query"))):command?(has(a,"inject","metacharacter")||(a.contains("shell")&&a.contains("command"))):has(a,"untrusted","input","inject","sensitive");
        boolean mechanism=sql?has(a,"parameter","prepared statement","bind"):nosql?has(a,"validat","schema","allowlist","allow-list","whitelist","type"):command?has(a,"argument list","processbuilder","avoid shell","allowlist","allow-list","whitelist"):has(a,"validat","parameter","encod","allowlist","allow-list");
        boolean prevention=(sql&&has(a,"separate","data","literal","not executable","query structure"))||(nosql&&has(a,"restrict","operator","structure","only allow"))||(command&&has(a,"not interpret","separate","argument","shell","metacharacter"))||(!sql&&!nosql&&!command&&has(a,"prevent","restrict","separate","cannot"));
        return new Result(root?5:0,mechanism?5:0,prevention?5:0,root?"You identified how untrusted input can affect the sensitive operation.":"Missing: explain the vulnerability's root cause.",mechanism?"You named an appropriate protective mechanism.":"Missing: name the appropriate prevention mechanism.",prevention?"You explained why the mechanism blocks or limits exploitation.":"Missing: explain why that mechanism prevents the attack.");
    }
    private static boolean has(String a,String... ts){for(String t:ts)if(a.contains(t))return true;return false;} private static String value(String v){return v==null?"":v;}
    record Result(int root,int mechanism,int prevention,String rootFeedback,String mechanismFeedback,String preventionFeedback) { }
}

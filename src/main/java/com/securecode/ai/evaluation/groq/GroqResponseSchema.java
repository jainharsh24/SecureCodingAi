// package com.securecode.ai.evaluation.groq;
// import java.util.List;
// import java.util.Map;
// final class GroqResponseSchema {
//     private GroqResponseSchema() { }
//     static final Map<String,Object> VALUE = Map.of("type","object","additionalProperties",false,"properties",Map.of(
//             "status",Map.of("type","string","enum",List.of("SAFE","VULNERABLE","UNCERTAIN")),
//             "expected_vulnerability",Map.of("type","string","enum",List.of("YES","NO","UNCERTAIN")),
//             "evidence",Map.of("type","string"),"severity",Map.of("type","string","enum",List.of("INFO","LOW","MEDIUM","HIGH","CRITICAL")),
//             "cwe",Map.of("type",List.of("string","null")),"confidence",Map.of("type","number","minimum",0,"maximum",1),"remediation",Map.of("type","string")),
//             "required",List.of("status","expected_vulnerability","evidence","severity","cwe","confidence","remediation"));
// }

package com.securecode.ai.evaluation.groq;

import java.util.List;
import java.util.Map;

final class GroqResponseSchema {
    private GroqResponseSchema() { }

    static final Map<String, Object> VALUE = Map.of(
        "type", "object",
        "additionalProperties", false,
        "properties", Map.of(
            "status", Map.of(
                "type", "string",
                "enum", List.of("SAFE", "VULNERABLE", "UNCERTAIN")
            ),
            "expected_vulnerability", Map.of(
                "type", "string",
                "enum", List.of("YES", "NO", "UNCERTAIN")
            ),
            "evidence", Map.of("type", "string"),
            "severity", Map.of(
                "type", "string",
                "enum", List.of("INFO", "LOW", "MEDIUM", "HIGH", "CRITICAL")
            ),
            "cwe", Map.of("type", List.of("string", "null")),
            "confidence", Map.of("type", "number"),
            "remediation", Map.of("type", "string")
        ),
        "required", List.of(
            "status",
            "expected_vulnerability",
            "evidence",
            "severity",
            "cwe",
            "confidence",
            "remediation"
        )
    );
}


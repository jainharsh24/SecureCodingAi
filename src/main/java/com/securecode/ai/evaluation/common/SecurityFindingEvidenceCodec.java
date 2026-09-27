package com.securecode.ai.evaluation.common;

import com.securecode.ai.evaluation.SecurityFinding;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/** Keeps concise persisted finding summaries independent from any evaluator's raw report format. */
@Component
public class SecurityFindingEvidenceCodec {
    private final JsonMapper jsonMapper;

    public SecurityFindingEvidenceCodec(JsonMapper jsonMapper) { this.jsonMapper = jsonMapper; }

    public String encode(List<SecurityFinding> findings) {
        try {
            return jsonMapper.writeValueAsString(findings.stream()
                    .map(finding -> new StoredSecurityFinding(label(finding), finding.cwe())).toList());
        } catch (Exception exception) { throw new IllegalStateException("Could not store security findings", exception); }
    }

    public List<StoredSecurityFinding> decode(String evidence) {
        if (evidence == null || evidence.isBlank()) return List.of();
        try { return List.copyOf(jsonMapper.readValue(evidence, new TypeReference<List<StoredSecurityFinding>>() { })); }
        catch (Exception ignored) { return List.of(); } // Existing reports used evaluator-specific raw formats.
    }

    private String label(SecurityFinding finding) {
        return finding.vulnerabilityType() == null || finding.vulnerabilityType().isBlank() ? finding.ruleId()
                : finding.vulnerabilityType();
    }
}

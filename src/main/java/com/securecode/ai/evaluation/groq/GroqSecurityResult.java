package com.securecode.ai.evaluation.groq;
import com.securecode.ai.evaluation.SecurityEvaluationStatus;
import com.securecode.ai.evaluation.SecurityFinding;
import java.util.List;
public record GroqSecurityResult(SecurityEvaluationStatus status,GroqAssessment assessment,List<SecurityFinding> findings,String error) { public GroqSecurityResult { findings=List.copyOf(findings); } }

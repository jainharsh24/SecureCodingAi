package com.securecode.ai.evaluation.groq;
public record GroqAssessment(String status,String expectedVulnerability,String evidence,String severity,String cwe,double confidence,String remediation) { }

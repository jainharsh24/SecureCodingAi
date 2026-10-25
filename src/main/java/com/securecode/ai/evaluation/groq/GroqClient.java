package com.securecode.ai.evaluation.groq;

import java.util.Map;
/** Transport boundary for Groq Chat Completions; prompts and security policy remain separate. */
public interface GroqClient {
    String complete(String systemPrompt, String userPrompt);
    default String complete(String systemPrompt, String userPrompt, String schemaName, Map<String, Object> schema) {
        return complete(systemPrompt, userPrompt);
    }
}

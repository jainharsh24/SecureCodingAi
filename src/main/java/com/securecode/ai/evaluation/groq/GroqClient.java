package com.securecode.ai.evaluation.groq;
/** Transport boundary for Groq Chat Completions; prompts and security policy remain separate. */
public interface GroqClient { String complete(String systemPrompt, String userPrompt); }

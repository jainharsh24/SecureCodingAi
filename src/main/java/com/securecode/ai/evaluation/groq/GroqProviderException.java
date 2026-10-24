package com.securecode.ai.evaluation.groq;
public class GroqProviderException extends RuntimeException {
    public enum Kind { AUTHENTICATION, RATE_LIMITED, TIMEOUT, PROVIDER, MALFORMED }
    private final Kind kind;
    public GroqProviderException(Kind kind, String message) { super(message); this.kind = kind; }
    public GroqProviderException(Kind kind, String message, Throwable cause) { super(message, cause); this.kind = kind; }
    public Kind kind() { return kind; }
}

package com.securecode.ai.service;

import org.springframework.stereotype.Component;

/** Compares Java submissions without treating whitespace-only or comment-only edits as attempts. */
@Component
public class JavaSourceEquivalence {
    public boolean equivalent(String left, String right) {
        return normalize(left).equals(normalize(right));
    }

    private String normalize(String source) {
        StringBuilder normalized = new StringBuilder();
        boolean previousWord = false;
        for (int index = 0; index < source.length();) {
            char current = source.charAt(index);
            if (Character.isWhitespace(current)) { index++; continue; }
            if (current == '/' && index + 1 < source.length() && source.charAt(index + 1) == '/') {
                index += 2;
                while (index < source.length() && source.charAt(index) != '\n' && source.charAt(index) != '\r') index++;
                continue;
            }
            if (current == '/' && index + 1 < source.length() && source.charAt(index + 1) == '*') {
                int end = source.indexOf("*/", index + 2);
                if (end < 0) return source; // Invalid Java: retain it so it cannot equal valid starter code.
                index = end + 2;
                continue;
            }
            if (current == '\"' || current == '\'') {
                int end = quotedEnd(source, index, current);
                if (end < 0) return source;
                if (previousWord) normalized.append(' ');
                normalized.append(source, index, end);
                previousWord = true;
                index = end;
                continue;
            }
            boolean word = Character.isJavaIdentifierPart(current);
            if (word && previousWord) normalized.append(' ');
            normalized.append(current);
            previousWord = word;
            index++;
        }
        return normalized.toString();
    }

    private int quotedEnd(String source, int start, char quote) {
        for (int index = start + 1; index < source.length(); index++) {
            if (source.charAt(index) == '\\') { index++; continue; }
            if (source.charAt(index) == quote) return index + 1;
        }
        return -1;
    }
}

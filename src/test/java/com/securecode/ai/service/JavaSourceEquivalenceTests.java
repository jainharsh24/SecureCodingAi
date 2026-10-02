package com.securecode.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JavaSourceEquivalenceTests {
    private final JavaSourceEquivalence equivalence = new JavaSourceEquivalence();

    @Test
    void ignoresWhitespaceAndCommentsButNotCodeChanges() {
        String starter = "class Main { // starter\n String value = \"same text\"; }";
        assertThat(equivalence.equivalent(starter, "class Main{/*changed comment*/ String value=\"same text\";}"))
                .isTrue();
        assertThat(equivalence.equivalent(starter, "class Main { String value = \"changed text\"; }"))
                .isFalse();
    }
}

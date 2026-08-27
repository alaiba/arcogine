package com.arcogine.challenge.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ChallengeDefinitionValidationResultTest {

    @Test
    void validFactoryProducesAnEmptyValidResult() {
        ChallengeDefinitionValidationResult result = ChallengeDefinitionValidationResult.valid();

        assertTrue(result.isValid());
        assertEquals(List.of(), result.issues());
    }

    @Test
    void nonEmptyIssuesAreNotValid() {
        ChallengeDefinitionValidationResult result = new ChallengeDefinitionValidationResult(
                List.of(new ChallengeDefinitionIssue("code", "path", "message")));

        assertFalse(result.isValid());
    }

    @Test
    void issueToStringIncludesPathCodeAndMessage() {
        ChallengeDefinitionIssue issue = new ChallengeDefinitionIssue("code", "path", "message");

        assertEquals("path [code]: message", issue.toString());
    }
}

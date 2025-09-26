Feature: Ambiguous steps
  Multiple step definitions that match a pickle step result in an AMBIGUOUS status, since Cucumnber cannot determine
  which one to execute.

  Scenario: Multiple step definitions for a step
    Given a step with multiple definitions

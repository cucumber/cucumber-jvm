Feature: Some scenarios should be run in serial

  @Serial
  Scenario: This one runs serially
    Given foo
    When foo
    Then baz

  Scenario: This one in run in parallel
    Given foo
    When foo
    Then baz

  Scenario: This one in run in parallel too
    Given foo
    When foo
    Then baz

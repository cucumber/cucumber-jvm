Feature: Feature B
# Scenario with same step occurring twice

  Scenario: Scenario B
    When foo
    Then bar

    When foo
    Then baz

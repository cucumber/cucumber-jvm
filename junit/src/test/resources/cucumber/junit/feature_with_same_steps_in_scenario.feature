Feature: Scenario with same step occurring twice
  Scenario:
    When foo
    Then bar
    
    When foo
    Then baz

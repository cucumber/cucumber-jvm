Feature: A feature with a single scenario

  @ResourceA  @ResourceAReadOnly
  Scenario: A single scenario
    Given a single scenario
    When it is executed
    Then is only runs once

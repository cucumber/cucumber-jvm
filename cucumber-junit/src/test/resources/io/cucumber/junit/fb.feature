Feature: Feature B

  Background:
    Given background step

  Scenario: A
    Then scenario name

  Scenario: B
    Then scenario name

  Scenario Outline: C
    Then scenario <name>
    Examples:
      | name |
      | C    |
      | D    |
      | E    |

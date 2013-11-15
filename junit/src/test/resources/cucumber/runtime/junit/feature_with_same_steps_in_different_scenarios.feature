Feature: In cucumber.junit
  Scenario: first
    When step
    Then another step

  Scenario: second
    When step
    Then another step

  Scenario Outline: third
    When <example> (step)
    Then another step

  Examples:
    | example       |
    | (example) 1.2 |
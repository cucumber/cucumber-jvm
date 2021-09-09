Feature: cucumber-guice supports singleton scope

  Scenario: cucumber-guice provides a singleton scope instance to multiple steps within the same scenario
    Given a singleton scope instance has been provided in this scenario
    When another singleton scope instance is provided
    And another singleton scope instance is provided
    Then all three provided instances are the same singleton instance

  Scenario: cucumber-guice provides the same instance for each scenario when singleton scope is specified for the object
    Given a singleton scope instance was provided in the previous scenario
    When another singleton scope instance is provided
    Then the two provided instances are the same instance

Feature: cucumber-guice supports scenario scope

  Scenario: cucumber-guice provides a scenario scope instance to multiple steps within the same scenario
    Given a scenario scope instance has been provided in this scenario
    When another scenario scope instance is provided
    And another scenario scope instance is provided
    Then all three provided instances are the same instance

  Scenario: cucumber-guice provides a new instance for each scenario when scenario scope is specified for the object
    Given a scenario scope instance was provided in the previous scenario
    When another scenario scope instance is provided
    Then the two provided instances are different
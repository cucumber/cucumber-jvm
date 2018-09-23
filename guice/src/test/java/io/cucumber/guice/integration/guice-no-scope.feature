Feature: cucumber-guice supports no scoping

  Scenario: cucumber-guice provides a new instance every time when no scope is specified for the object
    Given an un-scoped instance has been provided in this scenario
    When another un-scoped instance is provided
    And another un-scoped instance is provided
    Then all three provided instances are unique instances
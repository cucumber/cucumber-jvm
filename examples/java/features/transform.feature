@transform
Feature: Step argument transformations
  In order to maintain modularity within step definitions
  As a step definition editor
  I want to register a @Transform annotated method to capture and tranform step definition arguments.

  Scenario: transform with default match
    Given I pass '10' to a method with int as parameter
    When something happens
    Then all is good

  Scenario: transform with custom match
    Given I pass '10' to a method with User as parameter
    When something happens
    Then a User with age '10' is created

  Scenario: transform without match
    Given I pass '10' to a method with Car as parameter
    When something happens
    Then an exception is thrown

  Scenario: overriding a default transform
    Given I pass 'yes' to a method with boolean as parameter
    When something happens
    Then the parameter is true

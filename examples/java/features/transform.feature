@transform
Feature: Step argument transformations
  In order to maintain modularity within step definitions
  As a step definition editor
  I want to register a @Transform annotated method to capture and tranform step definition arguments.

  Scenario: transform with matches
    Given I pass '10' to a method with int as parameter
    When something happens
    Then all is good

      
  Scenario: transform without matches
    Given I pass '10' to a method with Car as parameter

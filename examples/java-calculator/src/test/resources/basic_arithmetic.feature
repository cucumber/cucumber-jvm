@foo
Feature: Basic Arithmetic
  Background: A Calculator
    Given a calculator I just turned on

  Scenario: Addition
    # Try to change one of the values below to provoke a failure
    When I add 4 and 5
    Then the result is 9

  Scenario: Addition
    # Try to change one of the values below to provoke a failure
    When I add 4 and 7
    Then the result is 11

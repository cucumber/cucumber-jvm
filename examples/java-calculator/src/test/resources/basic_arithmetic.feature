@foo
Feature: Basic Arithmetic
  Scenario: Addition
    # Try to change one of the values below to provoke a failure
    When I add 4 and 5
    Then the result is 9

  Scenario: Subtraction
    Given the following groceries:
      | name  | price |
      | milk  |     9 |
      | bread |     7 |
      | soap  |     5 |
    When I pay 25
    Then my change should be 4
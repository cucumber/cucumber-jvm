Feature: Calculate a result
  Perform an arithmetic operation on two numbers using a mathematical operator
  """The purpose of this feature is to illustrate how existing step-definitions
  can be efficiently reused."""

  Scenario Outline: Enter a digit, an operator and another digit
    Given I have a CalculatorActivity
    When I press <num1>
    And I press <op>
    And I press <num2>
    And I press =
    Then I should see <result> on the display

  Examples:
    | num1 | num2 | op | result   |
    | 9    | 8    | +  | 17.0     |
    | 7    | 6    | –  | 1.0      |
    | 5    | 4    | x  | 20.0     |
    | 3    | 2    | /  | 1.5      |
    | 1    | 0    | /  | Infinity |

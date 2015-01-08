Feature: Divide two numbers
  Calculate the quotient of two numbers which consist of one or more digits

  Scenario Outline: Enter one digit per number and press =
    Given I have a CalculatorActivity
    When I press <num1>
    And I press /
    And I press <num2>
    And I press =
    Then I should see <quotient> on the display

  Examples:
    | num1 | num2 | quotient |
    | 0    | 0    | NaN      |
    | 1    | 0    | Infinity |
    | 1    | 2    | 0.5      |

  Scenario Outline: Enter two digits per number and press =
    Given I have a CalculatorActivity
    When I press <num1>
    When I press <num2>
    And I press /
    And I press <num3>
    And I press <num4>
    And I press =
    Then I should see <quotient> on the display

  Examples:
    | num1 | num2 | num3 | num4 | quotient |
    | 2    | 2    | 2    | 2    | 1.0      |
    | 2    | 0    | 1    | 0    | 2.0      |

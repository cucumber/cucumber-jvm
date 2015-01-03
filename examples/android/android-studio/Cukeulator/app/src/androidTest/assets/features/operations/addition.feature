Feature: Add two numbers
  Calculate the sum of two numbers which consist of one or more digits

  Scenario Outline: Enter one digit per number and press =
    Given I have a CalculatorActivity
    When I press <num1>
    And I press +
    And I press <num2>
    And I press =
    Then I should see <sum> on the display

  Examples:
    | num1 | num2 | sum |
    | 0    | 0    | 0.0 |
    | 0    | 1    | 1.0 |
    | 1    | 1    | 2.0 |

  Scenario Outline: Enter two digits per number and press =
    Given I have a CalculatorActivity
    When I press <num1>
    When I press <num2>
    And I press +
    And I press <num3>
    And I press <num4>
    And I press =
    Then I should see <sum> on the display

  Examples:
    | num1 | num2 | num3 | num4 | sum   |
    | 0    | 0    | 2    | 0    | 20.0  |
    | 9    | 8    | 7    | 6    | 174.0 |

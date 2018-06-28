Feature: Subtract two numbers
  Calculate the difference of two numbers which consist of one or more digits

  Scenario Outline: Enter one digit per number and press =
    Given I have a CalculatorActivity
    When I press <num1>
    And I press –
    And I press <num2>
    And I press =
    Then I should see "<delta>" on the display

  Examples:
    | num1 | num2 | delta |
    | 0    | 0    | 0.0   |
    | 0    | 1    | -1.0  |
    | 1    | 2    | -1.0  |

  Scenario Outline: Enter two digits per number and press =
    Given I have a CalculatorActivity
    When I press <num1>
    When I press <num2>
    And I press –
    And I press <num3>
    And I press <num4>
    And I press =
    Then I should see "<delta>" on the display

  Examples:
    | num1 | num2 | num3 | num4 | delta |
    | 2    | 2    | 2    | 2    | 0.0   |
    | 2    | 0    | 1    | 0    | 10.0  |

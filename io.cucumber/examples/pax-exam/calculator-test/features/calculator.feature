Feature: Calculate a result

  Scenario Outline: Add two numbers
    Given I set a to <num1>
    Given I set b to <num2>
    When I call the calculator service
    Then the result is <result>

    Examples:
      | num1 | num2 | result |
      | 9    | 8    | 17     |
      | 5    | 4    | 9      |

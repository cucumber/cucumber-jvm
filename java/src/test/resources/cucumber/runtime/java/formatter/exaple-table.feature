Feature: Sample example table

  Scenario Outline: Testing rerun formatter
    Given The glue code faker is up
    When activity is triggered
    Then forcing the dummy glue code to <booleanCode>

  Examples:
    |booleanCode|
    |0          |
    |0          |
    |1          |
    |1          |
    |0          |



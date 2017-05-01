Feature: Test NG Scenarios

  Scenario: Regular Scenario
    Given Step1
    When Step2
    Then Step3

  Scenario Outline: Test Outline
    Given Step1 <text>
    When Step2
    Then Step3
    Examples:
      | text  |
      | text1 |
      | text2 |

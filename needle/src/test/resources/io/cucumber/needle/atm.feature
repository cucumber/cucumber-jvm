Feature: ATM

  Scenario: withdrawal
    Given I have 500 EUR in my account
    When I withdraw 200 EUR
    Then I have 300 EUR remaining.
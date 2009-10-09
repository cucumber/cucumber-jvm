# language: en
Feature: Division
  In order to avoid silly mistakes
  Cashiers must be able to calculate a fraction

  Scenario: A failure
    Given I have entered 4 into the calculator
    And I have entered 5 into the calculator
    When I press divide
    Then the current value should be 7
    And I should win the lotto

  Scenario: Regular numbers
    Given I have entered 3 into the calculator
    And I have entered 2 into the calculator
    When I press divide
    Then the current value should be 1.5
    And I should win the lotto

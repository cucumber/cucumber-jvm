# language: en
Feature: Division
  In order to avoid silly mistakes
  Cashiers must be able to calculate a fraction

  @important
  Scenario: Regular numbers
    Given I have entered 3 into the calculator
    And I have entered 2 into the calculator
    When I press divide
    Then the stored result should be 1.5

  Scenario: More numbers
    Given I have entered 6 into the calculator
    And I have entered 3 into the calculator
    When I press divide
    Then the stored result should be 2.0
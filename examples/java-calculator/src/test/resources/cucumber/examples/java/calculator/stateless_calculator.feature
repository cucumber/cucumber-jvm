Feature: Stateless Calculator

  Background: A Stateless Calculator
    Given I turned on the stateless calculator

  Scenario: Addition
    When I add 4 to 5
    Then The result should be 9
 
  Scenario: Subtraction
    When I subtract 21 from 56
    Then The result should be 35



Feature: Cukes without a belly

  Scenario: Have some cukes
    Given I have 4 cukes
    Then there are 4 cukes

  Scenario: Have some more cukes
    Given I have 6 cukes
    And I add 2 more cukes
    Then there are 8 cukes

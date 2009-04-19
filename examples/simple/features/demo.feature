Feature: Simple
  
  Scenario: 3 cukes
    Given I have 3 green cukes
    When I add a table
      |a|b|
      |1|2|
    Then I should have 3 green cukes
    
  Scenario: 4 cukes
    Given I have 4 green cukes
    When I add a table
      |a|b|
      |1|2|
    Then I should have 4 green cukes
    
  Scenario: 99 cukes
    Given I have 99 green cukes

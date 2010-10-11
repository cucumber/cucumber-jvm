Feature: Simple
  
  Scenario: 3 green cukes
    Given I have 3 green cukes
    When I add a table
      |a|b|
      |1|2|
    Then I should have 4 green cukes
    
  Scenario: 4 green cukes
    Given I have 4 green cukes
    When I add a table
      |a|b|
      |1|2|
    Then I should have 4 green cukes
  
  Scenario: 3 green and 4 yellow cukes
    Given I have 3 green cukes
    And I have 4 yellow cukes
    Then I should have 3 green cukes
    And I should have 4 yellow cukes
    
  Scenario: lots of green cukes
    Given I have 99 green cukes
    Then I should have 99 green cukes

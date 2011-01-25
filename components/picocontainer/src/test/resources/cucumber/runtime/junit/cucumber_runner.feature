Feature: Cucumber Runner Rocks
  Scenario: Many cukes
    Given I have 12 cukes in my belly
    And a big basket with cukes
    
  Scenario: Few cukes
    Given I have 3 cukes in my belly
    And I have 5 cukes in my belly
    
  Scenario Outline: Various things
    Given I have <n> cukes in my belly
    
    Examples: some cukes
      |  n |
      | 13 |
      |  4 |
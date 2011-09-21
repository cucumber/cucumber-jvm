Feature: Cucumber Runner Rocks
  @foo
  Scenario: Many cukes
    Given I have 12 cukes in my belly
    And a big basket with cukes
  
  Scenario: Few cukes
    Given I have 3 cukes in my belly
    Then there are 3 cukes in my belly
    
  Scenario Outline: Various things
    Given I have <n> <what> in my belly
    Then I should be <mood>
    
    Examples: some cukes
      |  n | what   | mood  |
      | 13 | cukes  | happy |
      |  4 | apples | tired |

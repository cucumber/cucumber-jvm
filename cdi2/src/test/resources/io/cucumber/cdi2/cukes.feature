Feature: Cukes

  Scenario: Eat some cukes
    Given I have 4 cukes in my belly
    Then there are 4 cukes in my belly

  Scenario: Eat some more cukes
    Given I have 6 cukes in my belly
    Then there are 6 cukes in my belly

  Scenario: Eat some unmanaged cukes
    Given I have 4 unmanaged cukes in my belly
    And I eat 2 more cukes
    Then there are 6 cukes in my belly

@focus
Feature: Cukes

  Scenario: Not cukes at all
    Given I have this in my basket:
      | a | b |
      | c | d |

  Scenario: Few cukes
    Given I have 3 cukes in my belly
    Then there are 3 cukes in my belly

  @gh210
  Scenario Outline: Various things
    Given I have <n> <what> in my belly
    Then I should be <mood>

    Examples: some cukes
      | n  | what   | mood  |
      | 13 | cukes  | happy |
      | 4  | apples | happy |
      | 8  | shots  | happy |

  @foo
  Scenario: Many cukes
    Given I have 12 cukes in my belly
    And a big basket with cukes
    And I have 12 cukes in my belly

  Scenario: An undefined step
    Given something undefined

  Scenario: A pending step
    Given something pending

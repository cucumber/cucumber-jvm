Feature: Cukes

  Scenario: in the belly
    Given I have 4 cukes in my belly
    Then there are 4 cukes in my belly

  Scenario: in the belly (list)
    Given I have this many cukes in my belly:
      | 13 |
    Then there are 13 cukes in my belly

  Scenario: unimplemented steps
    Given 5 unimplemented step

  @foo
  Scenario:
    Given I have 4 cukes in my belly

Feature: Cukes
  Scenario: in the belly
    Given I have 4 "cukes" in my belly
    Then I am "happy"

  Scenario: Optional arguments, argument present
    Given Something with an optional argument
    Then the argument should not be nil

  Scenario: Optional arguments, argument not present
    Given Something
    Then the argument should be nil

  Scenario: Calling step definition from another step
    Given a step called from another
    When I call that step
    Then the step got called
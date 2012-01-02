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

  Scenario: A step is defined as pending
    Given a pending step
    Then the pending step threw a pending exception

  Scenario: A step is defined as pending without any reason
    Given a reasonless pending step
    Then the pending step threw a pending exception without a reason
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

  Scenario: A stepdef is defined as pending
    Given a pending stepdef with reason "FIXME"
    Then the pending stepdef throws a pending exception with "FIXME"

  Scenario: A stepdef is defined as pending without any reason
    Given a pending stepdef without an explicit reason
    Then the pending stepdef throws a pending exception with "TODO"
    
  Scenario: Calling step definition from another step
    Given a step called from another
    When I call that step
    Then the step got called

  Scenario: Calling non existent step from another step
    When I call an undefined step from another
    Then I get an exception with "Undefined Step: When HOLY MOLEYS THIS DOESN'T EXIST!"
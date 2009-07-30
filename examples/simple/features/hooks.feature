Feature: Hooks
  
  Scenario: before should set b4
    Then b4 should have the value "b4 was here"

  Scenario: after should clean static
    Given static value is "cleaned"
    When I set static value to "dirty"
    Then static value should be "dirty"

  Scenario: next feature should be cleaned
    Given static value is "cleaned"

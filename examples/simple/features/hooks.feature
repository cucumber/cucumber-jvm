@b4
Feature: Tagged Hooks
  
  Scenario: before should set b4
    Then b4 should have the value "b4 was here"

  Scenario: after should clean static
    Then static value should be "clean"
    When I set static value to "dirty"
    Then static value should be "dirty"

  Scenario: next feature should be cleaned
    Then static value should be "clean"

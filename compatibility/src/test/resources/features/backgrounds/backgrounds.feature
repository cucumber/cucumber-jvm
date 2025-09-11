Feature: Backgrounds
  Though not recommended, Backgrounds can be used to share context steps between Scenarios. The Background steps
  are prepended to the steps in each Scenario when they are compiled to Pickles. Only one Background at the Feature
  level is supported.

  Background:
    Given an order for "eggs"
    And an order for "milk"
    And an order for "bread"

  Scenario: one scenario
    When an action
    Then an outcome

  Scenario: another scenario
    When an action
    Then an outcome
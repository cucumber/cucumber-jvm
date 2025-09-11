Feature: Rules with Backgrounds
  Like Features, Rules can also have Backgrounds, whose steps are prepended to those of each child Scenario. Only
  one Background at the Rule level is supported.

  It's even possible to have a Background at both Feature and Rule level, in which case they are concatenated.

  Background:
    Given an order for "eggs"
    And an order for "milk"
    And an order for "bread"

  Rule:
    Background:
      Given an order for "batteries"
      And an order for "light bulbs"

    Example: one scenario
      When an action
      Then an outcome

    Example: another scenario
      When an action
      Then an outcome
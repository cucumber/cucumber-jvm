Feature: Background

  Background: Can appear after feature
    Given a background

  Rule: Rules also have backgrounds
    Background: Can appear after rule
      And a and some more background

    Scenario: Both backgrounds are used
      Then three are 3 steps
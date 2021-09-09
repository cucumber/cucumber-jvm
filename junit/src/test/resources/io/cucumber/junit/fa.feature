Feature: Feature A

  Background: background
    Given first step

  Scenario: A good start
    Given first step
    Given second step
    Given third step


  Scenario Outline: Followed by some examples
    When <x> step
    Then <y> step
    Examples: examples 1 name
      | x      | y     |
      | second | third |
      | second | third |
    Examples: examples 2 name
      | x      | y     |
      | second | third |

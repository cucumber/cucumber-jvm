Feature: Stack
  In order to do stuff
  As a coder
  I want do simple arithmetic with a stack

  Scenario: Use Stack as Calculator
    Given I have an empty stack
    And I push + onto the stack
    And I push 1 onto the stack
    And I push 7 onto the stack
    When I evaluate the stack
    Then the top should be 8
    
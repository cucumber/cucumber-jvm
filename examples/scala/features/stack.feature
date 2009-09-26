Feature: Stack
  In order to do stuff
  As a coder
  I want do simple arithmetic with a stack

  Scenario: Addition
    Given I have an empty stack
    When I push 1 onto the stack
    And I push 7 onto the stack
    And I push + onto the stack
    Then the top should be 8
    And the size should be 1

  Scenario: Subtraction
    Given I have an empty stack
    When I push 2 onto the stack
    And I push 6 onto the stack
    And I push - onto the stack
    Then the top should be 4
    And the size should be 1

  Scenario: Multiplication
    Given I have an empty stack
    When I push 2 onto the stack
    And I push 1 onto the stack
    And I push 4 onto the stack
    And I push * onto the stack
    Then the top should be 8
    And the size should be 1

  Scenario: Division
    Given I have an empty stack
    When I push 2 onto the stack
    And I push 8 onto the stack
    And I push / onto the stack
    Then the top should be 4
    And the size should be 1
    
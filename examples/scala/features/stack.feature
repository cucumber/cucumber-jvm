Feature: Stack
  In order to do stuff
  As a coder
  I want do simple arithmetic with a stack

  Scenario Outline:
    Given I have an empty stack
    When I push <x> onto the stack
    And I push <y> onto the stack
    And I push <op> onto the stack
    Then the top should be <top>
    And the size should be 1

    Examples: 2 arguments
      | x | y | op | top |
      | 1 | 7 | +  | 8   |
      | 2 | 6 | -  | 4   |
      | 2 | 8 | /  | 4   |
      | 2 | 3 | *  | 6   |

  Scenario: Multiply 3 args
    Given I have an empty stack
    When I push 2 onto the stack
    And I push 1 onto the stack
    And I push 4 onto the stack
    And I push * onto the stack
    Then the top should be 8
    And the size should be 1
    
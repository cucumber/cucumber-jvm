Feature: Stack
  In order to do stuff
  As a coder
  I want do simple arithmetic with a stack

  Scenario Outline:
    Given I have an empty stack
    When I pøsh <x> onto the stack
    And I pøsh <y> onto the stack
    And I pøsh <op> onto the stack
    Then the top should be <top>
    And the size should be 1

    Examples: 2 arguments
      | x | y | op | top |
      | 1 | 7 | +  | 8   |
      | 6 | 2 | -  | 4   |
      | 8 | 2 | /  | 4   |
      | 2 | 3 | *  | 6   |

  Scenario: Multiply 3 args
    Given I have an empty stack
    When I pøsh 2 onto the stack
    And I pøsh 1 onto the stack
    And I pøsh 4 onto the stack
    And I pøsh * onto the stack
    Then the top should be 8
    And the size should be 1
    
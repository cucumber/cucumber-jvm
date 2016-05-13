Feature: Java8

  Scenario: use the API with Java8 style
    Given I have 42 cukes in my belly

  Scenario: another scenario which should have isolated state
    Given I have 42 cukes in my belly
    And something that isn't defined

  Scenario: use a table
    Given this data table:
      | first  | last     |
      | Aslak  | Hellesøy |
      | Donald | Duck     |

  Scenario: permute arity
    Given 0 args
    And 1 arg: a
    And 2 args: a b
    And 3 args: a b c
    And 4 args: a b c d
    And 5 args: a b c d e
    And 6 args: a b c d e f
    And 7 args: a b c d e f g
    And 8 args: a b c d e f g h
    And 9 args: a b c d e f g h i

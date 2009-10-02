Feature: Step argument transformations
  In order to maintain modularity within step definitions
  As a step definition editor
  I want to register a regex to capture and tranform step definition arguments.

  Scenario Outline: transform matches to different types
    Then I should transform '<value>' to <type>

  Examples:
    | value | type 	  |
    | 10    | Integer |
    | abc   | Symbol  |
    | 10    | Float   |
    | abc   | Array   |	
      
  Scenario: transform without matches
    Then I should not transform '10' to an Integer

@foo
Feature: Basic Arithmetic
  Background: A Calculator
    Given a calculator I just turned on

  Scenario: Addition
    # Try to change one of the values below to provoke a failure
    When I add 4 and 5
    Then the result is 9

  Scenario: Another Addition
    # Try to change one of the values below to provoke a failure
    When I add 4 and 7
    Then the result is 11

  Scenario Outline: Many additions
  	Given the following movements:
	  	|code| from| to|
		|A| | G|
		|A| G| R1|
		|A| R1| C1|
		|B| | G|
	When I add <a> and <b>    
    Then the result is <c>
     
     Examples: Single digits
       | a | b | c |
       | 1 | 2 | 3 |
       | 2 | 3 | 5 |

     Examples: Double digits
       |  a |  b |  c |
       | 10 | 20 | 30 |
       | 20 | 30 | 50 |

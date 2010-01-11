Feature: Book search
In order to find books I might buy
As a potential customer
I want to search for books by different criterias
  
  Background:
    Given the following books
        |Author     		|Title         										 |Year   |Publisher			|
        |Martin Fowler		|Patterns of Enterprise Application Architecture     |2002   |Addison Wesley	|
        |Eric Evans			|Domain Driven Design 				    			 |2003   |Addison Wesley	|
        |Gerard Meszaros	|xUnit Test Patterns                                 |2007   |Addison Wesley	|
  
  Scenario: Search for title
    When I search for title 'Patterns'
    Then the result list should contain 2 books
	
  Scenario: Search for author
    When I search for author 'Fowler'
    Then the result list should contain 1 book

  Scenario: Search for publisher
    When I search for publisher 'Addison Wesley'
    Then the result list should contain 3 books
    
  Scenario: Search for title and author
    When I search for author 'Fowler'
    And I search for title 'Patterns'
    Then the result list should contain 1 book

  Scenario: Search with no results
    When I search for author 'Evans'
    And I search for title 'Patterns'
    Then the result list should contain 0 books
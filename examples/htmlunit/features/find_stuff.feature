Feature: Find Stuff
  In order to become more knowledgeable
  Information seekers should easily find stuff
  
  Scenario Outline: Searching
    Given I'm standing on the Google search page
    When I search for <query>
    Then I should see a <text> link to <url>

    Examples: Actually find something
      | query | text | url |
      | cucumber | Cucumber - Wikipedia, the free encyclopedia |http://en.wikipedia.org/wiki/Cucumber|
  
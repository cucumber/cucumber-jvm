Feature: Temperature conversion
  
  Scenario: Celcius to Fahrenheit
    Given I am on the front page
    When I enter 0 Celcius
    Then I should see 32.0 Fahrenheit
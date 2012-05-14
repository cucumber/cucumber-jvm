Feature: Temperature conversion

  Scenario: 0 Celcius to Fahrenheit
    Given I am on the front page
    When I enter 0 Celcius
    Then I should see 32.0 Fahrenheit

  Scenario: 100 Celcius to Fahrenheit
    Given I am on the front page
    When I enter 100 Celcius
    Then I should see 212.0 Fahrenheit
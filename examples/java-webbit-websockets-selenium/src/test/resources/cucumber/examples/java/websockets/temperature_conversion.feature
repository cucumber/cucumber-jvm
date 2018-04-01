Feature: Temperature conversion

  Scenario: 0 Celsius to Fahrenheit
    Given I am on the front page
    When I enter 0 celsius
    Then I should see 32.0 fahrenheit

  Scenario: 100 Celcius to Fahrenheit
    Given I am on the front page
    When I enter 100 celsius
    Then I should see 212.0 fahrenheit

  Scenario: 100 Fahrenheit to Celsius
    Given I am on the front page
    When I enter 100 fahrenheit
    Then I should see 37.8 celsius
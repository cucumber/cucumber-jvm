Feature: Temperature conversion

  Scenario: 0 Celcius to Fahrenheit
    Given I am on the front page
    When I enter 0 celcius
    Then I should see 32.0 fahrenheit

  Scenario: 100 Celcius to Fahrenheit
    Given I am on the front page
    When I enter 100 celcius
    Then I should see 212.0 fahrenheit

  Scenario: 100 Fahrenheit to Celcius
    Given I am on the front page
    When I enter 100 fahrenheit
    Then I should see 37.8 celcius
Feature: Dates
  Scenario: A pretty date
    Given the date is 2011/10/25
    Then the date should be Oct 25 2011

  Scenario: An ISO 8601 date
    Given the iso date is 2012-03-01T06:54:14
    Then the date should be Mar 1 2012

  Scenario: An ISO 8601 date as Calendar
    Given the iso calendar is 2012-03-01T06:54:14
    Then the date should be Mar 1 2012

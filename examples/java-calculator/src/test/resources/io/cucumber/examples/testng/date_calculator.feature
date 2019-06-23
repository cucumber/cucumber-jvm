Feature: Dates with different date formats
  This feature shows you can have different date formats, as long as you annotate the
  corresponding step definition method accordingly.

  Scenario: Determine past date
    Given today is 2011-01-20
    When I ask if 2011-01-19 is in the past
    Then the result should be yes
Feature: Dates with different date formats
  This feature shows you can have different date formats, as long as you annotate the
  corresponding step definition method accordingly.

  Scenario: Determine past date
    Given today is Jan 20, 2011
    When I ask if Jan 19, 2011 is in the past
    Then the result should be yes
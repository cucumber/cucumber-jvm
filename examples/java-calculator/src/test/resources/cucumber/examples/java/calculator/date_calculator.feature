Feature: Dates with different date formats
  This feature shows you can have different date formats, as long as you annotate the
  corresponding step definition method accordingly. Notice that this currently does not work for
  List<Object> parameters (tables).

  Scenario: Determine past date
    Given today is 2011-01-02
    When I ask if 01/01/2011 is in the past
    Then the result should be yes
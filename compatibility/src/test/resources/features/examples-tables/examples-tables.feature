Feature: Examples Tables
  Sometimes it can be desirable to run the same scenario multiple times with
  different data each time - this can be done by placing an Examples table
  underneath a Scenario, and use <placeholders> in the Scenario which match the
  table headers.

  The Scenario Outline name can also be parameterized. The name of the resulting
  pickle will have the <placeholder> replaced with the value from the examples
  table.

  Scenario Outline: Eating cucumbers
    Given there are <start> cucumbers
    When I eat <eat> cucumbers
    Then I should have <left> cucumbers

    @passing
    Examples: These are passing
      | start | eat | left |
      |    12 |   5 |    7 |
      |    20 |   5 |   15 |

    @failing
    Examples: These are failing
      | start | eat | left |
      |    12 |  20 |    0 |
      |     0 |   1 |    0 |

    @undefined
    Examples: These are undefined because the value is not an {int}
      | start | eat    | left  |
      |    12 | banana |    12 |
      |     0 |      1 | apple |

  Scenario Outline: Eating cucumbers with <friends> friends
    Given there are <friends> friends
    And there are <start> cucumbers
    Then each person can eat <share> cucumbers

    Examples:
      | friends | start | share |
      |      11 |    12 |     1 |
      |       1 |     4 |     2 |
      |       0 |     4 |     4 |

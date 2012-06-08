Feature: Dates

  Scenario Outline: Parsing dates with simple a format (yyyy/MM/dd)
    Given the simple date is <input date>
    Then the date should be viewed in <timezone> as <year>, <month>, <day>, <hours>, <minutes>, <seconds>

  Examples:
    | input date | year | month | day | hours | minutes | seconds | timezone |
    | 2012/03/01 | 2012 | 3     | 1   | 0     | 0       | 0       | default  |

  Scenario Outline: Parsing dates with an ISO format (yyyy-MM-dd'T'HH:mm:ss)
    Given the ISO date is <input date>
    Then the date should be viewed in <timezone> as <year>, <month>, <day>, <hours>, <minutes>, <seconds>

  Examples:
    | input date          | timezone | year | month | day | hours | minutes | seconds |
    | 2012-03-01T06:54:14 | default  | 2012 | 3     | 1   | 6     | 54      | 14      |

  Scenario Outline: Parsing dates with a format including timezone (yyyy-MM-dd'T'HH:mm:ss, z)
    Given the ISO date with timezone is <input date>
    Then the date should be viewed in <timezone> as <year>, <month>, <day>, <hours>, <minutes>, <seconds>

  Examples:
    | input date               | timezone | year | month | day | hours | minutes | seconds |
    | 2008-12-31T23:59:59, PST | PST      | 2008 | 12    | 31  | 23    | 59      | 59      |
    | 2008-12-31T23:59:59, PST | UTC      | 2009 | 1     | 1   | 7     | 59      | 59      |

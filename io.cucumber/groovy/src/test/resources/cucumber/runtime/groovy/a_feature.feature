Feature: Cucumber Runner Rocks

  Scenario: Many cukes
    Given I have 12 cukes in my belly
    And a big basket with cukes

  Scenario: Few cukes
    Given I have 3 cukes in my belly
    And I have 5 cukes in my belly

  Scenario Outline: Various things
    Given I have <n> <what> in my belly
    Then I should be <mood>

  Examples: some cukes
    | n  | what   | mood  |
    | 13 | cukes  | happy |
    | 4  | apples | tired |

  Scenario: a table
    Given the following table:
      | year | name         |
      | 2008 | Cucumber     |
      | 2012 | Cucumber-JVM |

    Scenario: Test list conversion
      Given this should be converted to a list:Cucumber-JVM, Cucumber, Nice

  Scenario: A date
    Given today's date is "1971-10-03" and tomorrow is:
      """
      1971-10-04
      """


  Scenario: Call a method or property from second world
    When set world property "Hello"
    Then world property is "Hello"
    When world method call
    And world method call:
      | 1 |
      | 2 |
      | 3 |
    Then world method call is:
      | 1 |
      | 2 |
      | 3 |
    And properties visibility is ok

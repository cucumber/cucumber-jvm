Feature: Multiline
  Scenario: Table
    Given I have a table:
      | foo | bar |
      | 1   | 2   |
      | 3   | 4   |

  Scenario: String
    Given I have a string:
      """
      foo
      bar
      """
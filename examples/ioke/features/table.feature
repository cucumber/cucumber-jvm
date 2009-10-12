Feature: Table
  Scenario: Table
    Given I have some fine dudes:
      | who   | where   |
      | Ola   | Sweden  |
      | Sam   | England |
      | Aslak | Norway  |
    Then they should win the lotto

  Scenario: Table2
    Given I have another set of some fine dudes:
      | who   | where   |
      | Ola   | Sweden  |
      | Sam   | England |
      | Aslak | Norway  |
    Then they should win the lotto

  Scenario: Strings
    Given I have a really long String:
      """
      OMG
      So
      big!
      """
    Then just say something to make cucumber fail
    
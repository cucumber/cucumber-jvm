Feature: With everything

  Scenario: A single scenario
    Given a single scenario
    When it is executed
    Then nothing else happens

  @ScenarioOutlineTag
  Scenario Outline: A scenario outline
    Given a scenario outline
    When it is executed
    Then <example> is used

    @Example1Tag
    Examples: With some text
      | example |
      | A       |
      | B       |

    @Example2Tag
    Examples: With some other text
      | example |
      | C       |
      | D       |

  @ScenarioOutlineTag
  Scenario Outline: A scenario outline with one example
    Given a scenario outline
    When it is executed
    Then <example> is used

    @Example1Tag
    Examples:
      | example |
      | A       |
      | B       |

  Rule: A rule

    Example: An example of this rule
      Given a single scenario
      When it is executed
      Then nothing else happens

    Example: An other example of this rule
      Given a single scenario
      When it is executed
      Then nothing else happens


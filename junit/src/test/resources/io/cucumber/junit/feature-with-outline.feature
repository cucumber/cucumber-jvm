@FeatureTag
Feature: A feature with scenario outlines

  @ScenarioTag @ResourceA  @ResourceAReadOnly
  Scenario: A scenario
    Given a scenario
    When it is executed
    Then is only runs once

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

@FeatureTag
Feature: A feature with scenario outlines

  @ScenarioTag
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
    Examples:
       | A |
       | B |
       | C |

    @Example2Tag
    Examples:
      | D |
      | E |
      | F |

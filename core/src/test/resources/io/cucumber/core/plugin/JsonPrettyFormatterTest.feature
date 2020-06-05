Feature: Feature_3

  Background:
    Given bg_1
    When bg_2
    Then bg_3

  Scenario: Scenario_1
    Given step_1
    When step_2
    Then step_3
    Then cliché

  Scenario Outline: ScenarioOutline_1
    Given so_1 <a>
    When so_2 <c> cucumbers
    Then <b> so_3

    Examples:
      | a  | b | c  |
      | 12 | 5 | 7  |
      | 20 | 5 | 15 |

  Scenario: Scenario_2
    Given a
    Then b
    When c


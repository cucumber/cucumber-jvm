Feature: Feature executed in parallel

  Background:
    Given bg_1_parallel
    When bg_2_parallel
    Then bg_3_parallel

  Scenario: Scenario_1
    Given step_1_parallel
    When step_2_parallel
    Then step_3_parallel
    Then cliché_parallel

  Scenario Outline: ScenarioOutline_1_parallel
    Given so_1 <a>_parallel
    When so_2 <c> cucumbers_parallel
    Then <b> so_3_parallel

    Examples:
      | a  | b | c  |
      | 12 | 5 | 7  |
      | 20 | 5 | 15 |

  Scenario: Scenario_2
    Given a_parallel
    Then b_parallel
    When c_parallel


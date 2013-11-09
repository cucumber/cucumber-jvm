Feature: Skip Tags

  Scenario: Ok
    Given skip_tag_step_1
    When skip_tag_step_2
    Then skip_tag_step_3

  @Skip
  Scenario: Skipped
    Given skip_tag_step_1
    When skip_tag_step_2
    Then skip_tag_step_3

  Scenario Outline: ScenarioOutline_1
    Given skip_tag_step_1 <skip>
    When skip_tag_step_2 <skip>
    Then skip_tag_step_3 <skip>

  Examples:
    | skip |
    | Ok   |

  @Skip
  Examples:
    | skip    |
    | Skipped |

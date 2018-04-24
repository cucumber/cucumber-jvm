Feature: Feature_Sync_1_File_2

  Background:
    Given bg_1
    When bg_2
    Then bg_3
  
  @synchronized-1
  Scenario: Scenario_1
    Given step_1
    When step_2
    Then step_3

Feature: Hooks
  Hooks are special steps that run before or after each scenario's steps.

  Scenario: No tags and a passed step
    When a step passes

  Scenario: No tags and a failed step
    When a step fails

  Scenario: No tags and a undefined step
    When a step does not exist

Feature: Global hooks
  Hooks can be at the test run level, so they run once before or after all test cases.

  AfterAll hooks are executed in reverse order of definition.

  Scenario: A passing scenario
    When a step passes

  Scenario: A failing scenario
    When a step fails

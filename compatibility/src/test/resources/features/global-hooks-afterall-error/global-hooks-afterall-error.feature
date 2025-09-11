Feature: Global hooks - AfterAll error
  Errors in AfterAll hooks cause the whole test run to fail. The remaining AfterAll hooks will still run, in an
  effort to clean up resources as well as possible.

  Scenario: A passing scenario
    When a step passes

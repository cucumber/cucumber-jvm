Feature: Global hooks - BeforeAll error
  Errors in BeforeAll hooks cause the whole test run to fail. Test cases will not be executed. The remaining BeforeAll
  hooks will still run, along with all AfterAll hooks, in an effort to clean up resources as well as possible.

  Scenario: A passing scenario
    When a step passes

Feature: Unused steps
  Depending on the run, some step definitions may not be used. This is valid, and the step definitions are still
  includes in the stream of messages, which allows formatters to report on step usage if desired.

  Scenario: a scenario
    Given a step that is used

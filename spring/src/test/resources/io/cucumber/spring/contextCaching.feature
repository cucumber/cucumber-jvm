Feature: context caching with JUnit tests

  Scenario: There can only be one application context
    When I run a scenario
    Then there should be only one Spring context

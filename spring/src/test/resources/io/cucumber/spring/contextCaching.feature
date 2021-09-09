Feature: context caching with JUnit tests

  Scenario: There can only be one application context
    When I run a scenario in the same JVM as the SharedContextTest
    Then there should be only one Spring context

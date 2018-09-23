Feature: Spring Threading Cukes
  In order to have a completely clean system for each scenario
  As a purity activist
  I want that beans have both scenario and thread scope.

  Scenario: A parallel execution
    Given I am a step definition
    When when executed in parallel
    Then I should not be shared between threads

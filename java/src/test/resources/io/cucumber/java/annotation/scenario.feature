Feature: Scenario information is available during step execution

  Scenario: My first scenario
    Given I am running a scenario
    When I try to get the scenario name
    Then The scenario name is "My first scenario"

  Scenario: My second scenario
    Given I am running a scenario
    When I try to get the scenario name
    Then The scenario name is "My second scenario"

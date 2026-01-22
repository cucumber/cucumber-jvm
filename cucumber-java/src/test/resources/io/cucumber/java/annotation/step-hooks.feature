Feature: Step information is available in step hooks

  Scenario: BeforeStep hook receives step information
    Given I have a step to execute
    When I execute another step
    Then the BeforeStep hook captured the correct step info

  Scenario: AfterStep hook receives step information
    Given I have a step to execute
    When I execute another step
    Then the AfterStep hook captured the correct step info

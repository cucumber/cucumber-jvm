Feature: Script Setup

  Scenario: Setup method is invoked if present in a script
    When a script with setup method is under use
    Then setup must be invoked once

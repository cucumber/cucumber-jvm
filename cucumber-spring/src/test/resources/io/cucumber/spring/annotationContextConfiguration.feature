Feature: context configuration with @CucumberContextConfiguration annotation

  Scenario: Spring configuration is picked up, when no step definitions are present
    Then cucumber picks up configuration class without step defs

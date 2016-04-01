@global
Feature: Feature information is available during step execution

  Scenario: Feature Name
    Given I am running a feature
    When I try to get the feature name
    Then The feature name is "Feature information is available during step execution"
    And The scenario name is "Feature Name"

  Scenario: Feature Name - negative
    Given I am running a feature
    When I try to get the feature name
    Then The feature name is not "Feature Name - negative"
    And The scenario name is "Feature Name - negative"

  @feature
  Scenario: Feature tag
    Given I am running a feature
    When I try to get the feature tag
    Then The feature tag is "@global"
    And The feature tag is "@feature"
    And The feature tag is not "@scenario"
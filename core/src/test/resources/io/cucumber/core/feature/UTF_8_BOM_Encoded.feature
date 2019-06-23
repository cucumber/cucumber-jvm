#Sample comment

Feature: UTF-8 BOM feature file

  Scenario: Pass UTF-8-BOM file
    Given that I created UTF-8-BOM encoded feature-file
    When I pass it to cucumber-jvm
    Then it gets parsed normally

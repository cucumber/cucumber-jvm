#Sample comment

Feature: UTF-8 Feature file

  Scenario: Pass UTF file
    Given that I created UTF-8 encoded feature-file
    When I pass it to cucumber-jvm
    Then it gets parsed normally

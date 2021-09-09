Feature: Doc String

  Scenario: This is valid Gherkin
    Given an doc string
      """
      This doc string has no content type
      """
    Given an doc string with content type
      """text/plain
      This doc string has content a type
      """

Feature: Spring web contextrenam injection with annotations

  Scenario: Inject web context
    Given I have the web context set
    When I call the url "/test"
    Then it should return 200

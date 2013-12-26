Feature: Soft nature
  Background: Difficult childhood
    When Batman was a child
    Then his parents were murdered

  Scenario: Batman cries when asked about parents
    Given I'm Batman
    When Robin says "Where are your parents?"
    Then I cry

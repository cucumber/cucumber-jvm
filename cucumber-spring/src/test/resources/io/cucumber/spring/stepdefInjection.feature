Feature: StepDef injection

  Scenario: StepDef injection
    Given the StepDef injection works
    When I assign the "cucumbers" attribute to 4 in one step def class
    Then I can read 4 cucumbers from the other step def class
    And 4 have been pushed to a third step def class

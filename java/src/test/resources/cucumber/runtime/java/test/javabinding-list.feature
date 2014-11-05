Feature: Java Binding

  Scenario: Doing something
    Given I do something
    Then I should get
      | name          | foo | bar |
      | amount        | 1   | 2   |
    And I should have gotten "2" items

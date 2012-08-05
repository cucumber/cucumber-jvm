Feature: Hello World

  Scenario: Say hello
    Given I have a hello app with "Howdy"
    When I ask it to say hi
    Then it should answer with "Howdy World"

  Scenario: Print my shopping list
    The list should be printed in alphabetical order of the item names

    Given a shopping list:
      | name  | count |
      | Milk  |     2 |
      | Cocoa |     1 |
      | Soap  |     5 |
    When I print that list
    Then it should look like:
      """
      1 Cocoa
      2 Milk
      5 Soap

      """

  Scenario: Transformation
    Given today is "Dec 6, 2012"
    And I did laundry 2 days ago
    Then my laundry day must have been "Dec 4, 2012"

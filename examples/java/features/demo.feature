Feature: Simple

  Scenario: 3 green cukes
    Given I have 3 green cukes
    When I add a table
      |a|b|
      |1|2|
    Then I should have 3 green cukes
    And I should have 0 yellow cukes

  Scenario: 4 green cukes
    Given I have 4 green cukes
    When I add a table
      |a|b|
      |1|2|
    And I add a string
    """
    Hello
    World
    """
    Then I should have 4 green cukes

  Scenario: 3 green and 4 yellow cukes
    Given I have 3 green cukes
    And I have 4 yellow cukes
    Then I should have 3 green cukes
    And I should have 4 yellow bananas

  Scenario: 99 green cukes
    Given I have 99 green cukes
    Then I should have 99 green cukes

  Scenario: Let's try pending
    Given a pending step
    And a failing step that is preceded by a pending

  Scenario: Mapping tables
    Given a table that we convert:
      |a    |b     |
      |eenie|meenie|
      |miney|moe   |

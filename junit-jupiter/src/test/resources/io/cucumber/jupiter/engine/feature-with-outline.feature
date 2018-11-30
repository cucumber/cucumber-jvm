Feature: A feature with scenario outlines

  Scenario: A scenario
    Given a scenario
    When it is executed
    Then is only runs once

  Scenario Outline: A scenario outline
    Given a scenario outline
    When it is executed
    Then <example> is used

    Examples:
       | A |
       | B |
       | C |

    Examples:
      | D |
      | E |
      | F |

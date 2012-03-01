Feature: Java Enums

  Scenario Outline: color should be recognized as an enum
    Given I want to recognize colors as enums
    When i use the <color> in a step
    Then it should be recognized as enum

  Examples:
    | color |
    | Red   |
    | red   |
    | RED   |
    | ReD   |


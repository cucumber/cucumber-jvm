Feature: Java Enums

  Background: What we want
    Given I want to recognize colors as enums

  Scenario Outline: color should be recognized as an enum
    When i use the <color> in a step
    Then it should be recognized as enum

  Examples:
    | color |
    | Red   |
    | red   |
    | RED   |
    | ReD   |


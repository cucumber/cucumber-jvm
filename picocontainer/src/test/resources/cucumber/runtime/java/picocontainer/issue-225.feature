Feature: Issue 225

  Scenario Outline: Outline 1
    When foo
    Then bar concerning <fluffy thing>

  Examples:
    | fluffy thing         |
    | a fluffy spiked club |

  Scenario Outline: Outline 2
    When foo
    Then bang bang concerning <spiky thing>

  Examples:
    | spiky thing          |
    | a fluffy spiked club |
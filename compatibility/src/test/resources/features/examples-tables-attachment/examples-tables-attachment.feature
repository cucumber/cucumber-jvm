Feature: Examples Tables - With attachments
  It is sometimes useful to take a screenshot while a scenario runs or capture some logs.

  Scenario Outline: Attaching images in an examples table
    When a <type> image is attached

    Examples:
      | type |
      | JPEG |
      | PNG  |

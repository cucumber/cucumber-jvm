Feature: Examples Tables - With attachments
  It is sometimes useful to take a screenshot while a scenario runs.
  Or capture some logs.

  This can also be done in an examples table.

  Scenario Outline: Attaching images in an examples table
    When a <type> image is attached

    Examples:
      | type |
      | JPEG |
      | PNG  |

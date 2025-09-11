Feature: Doc strings
  Doc strings are a way to supply long, sometimes multi-line, text to a step. They are passed as the last argument
  to the step definition.

  Scenario: a doc string with standard delimiter
    Three double quotes above and below are the standard delimiter for doc strings.

    Given a doc string:
    """
    Here is some content
    And some more on another line
    """

  Scenario: a doc string with backticks delimiter
    Backticks can also be used, like Markdown, but are less widely supported by editors.

    Given a doc string:
    ```
    Here is some content
    And some more on another line
    ```

  Scenario: a doc string with media type
    The media type can be optionally specified too, following the opening delimiter.

    Given a doc string:
    """application/json
    {
      "foo": "bar"
    }
    """
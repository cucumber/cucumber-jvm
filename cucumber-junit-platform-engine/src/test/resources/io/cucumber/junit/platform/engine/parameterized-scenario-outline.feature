Feature: A feature with a parameterized scenario outline

  Scenario Outline: A scenario full of <vegetable>s
    Given a scenario outline

    @Example1Tag
    Examples: Of the Gherkin variety
      | vegetable |
      | Cucumber  |
      | Zucchini  |

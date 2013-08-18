Feature: Cukes

  Scenario Outline: Eat some cukes
    Given I have <numberOfBeans> cukes in my belly
    Then there are <numberOfBeans> cukes in my belly

    Examples:
    | numberOfBeans |
    | 4             |
    | 2             |

Feature: Dirty Cukes

  Scenario Outline: Eat some dirty cukes
    Given I have <numberOfBeans> dirty cukes in my belly
    Then there are <numberOfBeans> dirty cukes in my belly

    Examples:
    | numberOfBeans |
    | 4             |
    | 2             |

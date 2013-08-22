Feature: Spring based test where each scenario results in a refresh of the spring context

  Scenario Outline: Eat some dirty beans
    Given I have <numberOfBeans> dirty beans in my belly
    Then there are <numberOfBeans> dirty beans in my belly

  Examples:
    | numberOfBeans |
    | 4             |
    | 2             |

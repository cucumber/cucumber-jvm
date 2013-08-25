Feature: Spring Dirty Cukes
  In order to have a completely clean system for each scenario
  As a purity activist
  I want each dirty scenario to have its own application context

  Scenario Outline: Eat some annotated dirty cukes
    Given I have <numberOfBeans> dirty cukes in my belly
    Then there are <numberOfBeans> dirty cukes in my belly

    Examples:
    | numberOfBeans |
    | 4             |
    | 2             |

  Scenario Outline: Eat some XML dirty beans
      Given I have <numberOfBeans> dirty beans in my belly
      Then there are <numberOfBeans> dirty beans in my belly

    Examples:
      | numberOfBeans |
      | 4             |
      | 2             |

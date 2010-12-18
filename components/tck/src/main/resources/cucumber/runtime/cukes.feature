Feature: Cukes
  Scenario: 1 cuke
    Given I have 5 cukes in my belly
    Then there are 4 cukes in my belly

  Scenario Outline: cooking
    Given the <container> contains <ingredient>
    When I add <liquid>
    And serve it to my guests
    Then they'll be eating <dish>

    Examples:
      | container | ingredient | liquid    | dish         |
      | bowl      | oats       | milk      | oatmeal      |
      | glass     | guinness   | champagne | black velvet |

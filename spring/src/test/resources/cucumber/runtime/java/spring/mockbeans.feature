Feature: Mocked belly

  In order to mock belly beans

  Scenario Outline: Mock
    Given a mocked belly is not a real belly
    When the belly is mocked to contain <count> cukes
    Then the belly contains <count> cukes

    Examples:
      | count |
      | 3     |
      | 4     |
      | 5     |
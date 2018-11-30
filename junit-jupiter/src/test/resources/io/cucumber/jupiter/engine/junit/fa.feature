Feature: Feature A

  Scenario: Scenario A
    Given GA
    When GA
    Then TA

  Scenario Outline: Scenario A
    Given G<A>
    When G<A>
    Then T<A>

    Examples:
       | A |
       | B |
       | C |

    Examples:
      | A |
      | E |
      | F |

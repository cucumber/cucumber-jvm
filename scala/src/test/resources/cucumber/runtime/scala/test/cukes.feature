Feature: Cukes

  Scenario: in the belly
    Given I have 4 "cukes" in my belly
    Then I am "happy"

  Scenario: greedy in the belly
    Given I have the following foods :
      | FOOD   | CALORIES |
      | cheese |      500 |
      | burger |     1000 |
      | fries  |      750 |
    Then I am "definitely happy"

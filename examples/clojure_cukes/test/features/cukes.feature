Feature: Cukes
  An example of testing Clojure with cucumber. The second 'Then' of
      'eat 1 cuke' is failing. Change the "happy" to "meh" to make
      it pass.

  Scenario: in the belly
    Given I have 4 big "cukes" in my belly
    Then I am "happy"

  Scenario: eat 1 cuke
    Given I have 0 big "cukes" in my belly
    Then I am "hungry"
    When I eat 1 "cukes"
    Then I am "sad"
    When I eat 1 "cukes"
    Then I am "happy"

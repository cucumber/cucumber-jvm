Feature: Cukes
  An example of testing Clojure with cucumber. To see a failure try changing
  the last 'Then' of 'eat 1 cuke' "meh" to "happy".

  Scenario: in the belly
    Given I have 4 big "cukes" in my belly
    Then I am "happy"

  Scenario: eat 1 cuke
    Given I have 0 big "cukes" in my belly
    Then I am "hungry"
    When I eat 1 "cukes"
    Then I am "sad"
    When I eat 1 "cukes"
    Then I am "meh"

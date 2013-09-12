Feature: Cukes

  Scenario: in the belly
    Given I have 4 "cukes" in my belly
    Then there are 4 "cukes" in my belly

  @bellies 
  Scenario: in the belly of Doggie, testing hooks with tags
    Given Doggie has 4 "cukes" in his belly
    Then there are 4 "cukes" in the belly of Doggie

  Scenario: in the belly of Doggie, testing hooks with no tags
    Given Doggie has 4 "cukes" in his belly
    Then I wake up and there is no Doggie

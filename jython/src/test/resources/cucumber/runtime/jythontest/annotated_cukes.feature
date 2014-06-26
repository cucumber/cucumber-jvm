Feature: Cukes

  Scenario: default cukes in the belly from a tagless Before annotation
    Given I have "5" cukes in my belly

  @must_have_more_cukes
  @and_then_some

  Scenario: more cukes in the belly from tagged and tagless Before annotations
    Given I have "6" cukes in my belly


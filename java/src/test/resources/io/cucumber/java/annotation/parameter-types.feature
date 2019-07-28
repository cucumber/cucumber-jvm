Feature: ParameterTypes

  Scenario: Convert a parameter to date via the ParameterTypeRegistry
    Given tomorrow is 1907/11/14

  Scenario: Convert a parameter to date via the @ParameterType Annotation
    Given today is 1907-11-14

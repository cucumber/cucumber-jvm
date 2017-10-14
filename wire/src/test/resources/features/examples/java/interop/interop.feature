Feature: Library Interoperability

  As a library user
  I want to be able to exchange data between two different language endpoints

  Background:

    Given a publisher & subscriber with compatible endpoint configurations

  Scenario Outline:  Interoperability test between endpoints

    Given a <api1> subscriber
    And a <api2> publisher
    When the <api1> publisher publishes <data>
    Then the <api2> subscriber receives <data>

    Examples:

      | api1 | api2 | data  |
      | Java | Java | foo   |

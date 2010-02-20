@ask
Feature: Half manual
  In order to speed up manual tests
  Testers should at least be able to automate parts of it

  Scenario: Get some manual inout
    When I ask for input
    Then it should time out

Feature: Billing transactions

  Scenario: Send transactions to billing
    Given I have a transaction
    When I send the transaction to billing
    Then the response should be OK

  Scenario: Send transactions to billing again
    Given I have a transaction
    When I send the transaction to billing
    Then the response should be OK

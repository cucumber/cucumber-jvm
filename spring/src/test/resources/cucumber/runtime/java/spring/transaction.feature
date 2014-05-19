@txn
Feature: Transaction feature

  Scenario: Transaction scenario
    Given a feature with the @txn annotation
    Then the scenarios shall execute within a transaction

# The @txn tag enables a Transaction open-rollback around each Scenario,
# Preventing persisted data from leaking between Scenarios.
# Try removing the @txn tag and see what happens.
@txn
Feature: Clean slates

  Scenario: Gaia and Aslak have cukes for breakfast
    Given we had this for breakfast:
      | name  | cukes |
      | Gaia  | 3     |
      | Aslak | 8     |
    Then the total number of breakfasts should be 2

  Scenario: Gaia and Patty have cukes for breakfast
    Given we had this for breakfast:
      | name  | cukes |
      | Gaia  | 9     |
      | Aslak | 80    |
    Then the total number of breakfasts should be 2

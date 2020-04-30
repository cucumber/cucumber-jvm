# The @txn tag enables a Transaction open-rollback around each Scenario,
# Preventing persisted data from leaking between Scenarios.
# Try removing the @txn tag and see what happens.
@txn
Feature: Search

  Background:
    Given there is a user

  Scenario: Find messages by content
    Given a User has posted the following messages:
      | content            |
      | I am making dinner |
      | I just woke up     |
      | I am going to work |
    When I search for "I am"
    Then the results content should be:
      | I am making dinner |
      | I am going to work |

  Scenario: Find messages by content
    Given a User has posted the following messages:
      | content              |
      | I just woke up again |
    When I search for "woke up"
    Then the results content should be:
      | I just woke up again |

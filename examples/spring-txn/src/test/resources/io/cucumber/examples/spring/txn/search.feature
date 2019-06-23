# The @txn tag enables a Transaction open-rollback around each Scenario,
# Preventing persisted data from leaking between Scenarios.
# Try removing the @txn tag and see what happens.
@txn
Feature: Search

  Scenario: Find messages by content
    Given a User has posted the following messages:
      | content            |
      | I am making dinner |
      | I just woke up     |
      | I am going to work |

  Scenario: Find messages by content using auto-search
    Given a User has posted the following messages:
      | content            |
      | I am making dinner |
      | I just woke up     |
      | I am going to work |

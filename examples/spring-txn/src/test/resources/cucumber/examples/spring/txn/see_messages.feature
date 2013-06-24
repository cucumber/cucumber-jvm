@txn
Feature: See Messages

  Scenario: See another user's messages
    Given there is a User
    And the User has posted the message "this is my message"
    When I visit the page for the User
    Then I should see "this is my message"

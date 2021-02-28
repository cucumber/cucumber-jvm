Feature: See Messages

  Scenario: See another user's messages
    Given there is a user
    And the user has posted the message "this is my message"
    When I visit the page for the User
    Then I should see "this is my message"

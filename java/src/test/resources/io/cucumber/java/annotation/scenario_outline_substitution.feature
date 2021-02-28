Feature: Scenario Outline Substitution

  Scenario Outline: Email confirmation
    Given I have a user account with my name "Jojo Binks"
    When an Admin grants me <Role> rights
    Then I should receive an email with the body:
    """
    Dear Jojo Binks,
    You have been granted <Role> rights.  You are <details>. Please be responsible.
    -The Admins
    """
    Examples:
      | Role    | details                                       |
      | Manager | now able to manage your employee accounts     |
      | Admin   | able to manage any user account on the system |

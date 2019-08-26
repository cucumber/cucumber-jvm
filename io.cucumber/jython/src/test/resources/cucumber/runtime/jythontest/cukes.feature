Feature: Cukes

  Scenario: in the belly
    Given I have 4 "cukes" in my belly
    Then I am "happy"
  
  Scenario: DataTable
  Given the following users exist:
    | name  | email           | phone |
    | Aslak | aslak@email.com | 123   |
    | Matt  | matt@email.com  | 234   |
    | Joe   | joe@email.org   | 456   | 

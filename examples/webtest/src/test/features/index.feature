Feature: Index
  In order validate my webapp works as intended
  As a web developer
  I want to view the index page

  Scenario: See some expected information
    Given I am on the index page
    Then I should see "Have a cuke!"
    And I should see a link to "http://cukes.info"
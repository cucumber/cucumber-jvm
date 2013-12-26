Feature: Millionaire
  Background: Batman is rich
    Given Batman's parents were rich

  Scenario: Batman can buy expensive cars
    Given I'm Batman
    When I buy 10 Rolls Royce "Phantoms"
    Then my bank account balance is positive

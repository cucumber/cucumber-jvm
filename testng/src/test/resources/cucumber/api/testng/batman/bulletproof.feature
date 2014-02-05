Feature: Bulletproof costume

  Scenario: Bullets fired at Batman
    Given I'm Batman
    And I'm wearing my bat costume
    When The Joker fires at me
    Then bullets fly the other direction out of sheer terror

Feature: Talk to the world
  
  Scenario: Talk to the world
    Given I have a world
    When I tell the world to say hello
    Then the response should be Have a cuke, Duke!

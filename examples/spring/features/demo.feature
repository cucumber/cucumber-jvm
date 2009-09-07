Feature: Talk to the world
  
  Scenario: Talk to the world
    Given I have a greeter
    When I tell the greeter to say hello
    Then the response should be "Have a cuke, Duke!"

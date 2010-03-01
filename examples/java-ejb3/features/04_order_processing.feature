Feature: Order processing
  In order to get satisfied customers
  As the shop owner
  I want orders to be processed automatically
  
  Scenario: Order gets processed automatically after creation
    Given a newly submitted order
    When I wait 1s
    Then the order status should be 'InProgress'
    ### To enable Timer see OrderProcessor.java. It works, but embedded container is not shut down properly
    #When I wait 1s
    #Then the order status should be 'Closed' 


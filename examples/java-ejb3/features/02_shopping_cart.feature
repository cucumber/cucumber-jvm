Feature: Shopping cart
In order to create an order
As a customer
I want to put books into my shopping cart
  
  Scenario: Putting one book into the shopping cart
    When I put a book with price 11.5 into my shopping cart
    Then my shopping cart should contain 1 line item
    And the total price should be 11.5
    
  Scenario: Putting two books into the shopping cart
    When I put a book with price 11.2 into my shopping cart
    And I put a book with price 22.3 into my shopping cart
    Then my shopping cart should contain 2 line items
    And the total price should be 33.5

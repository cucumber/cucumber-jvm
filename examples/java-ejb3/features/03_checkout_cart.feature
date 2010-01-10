Feature: Checkout cart
In order to buy books
As a customer
I want to checkout my cart and commit the order.
  
  Scenario: Committing a simple order
    Given my shopping cart contains 1 book with price 11.5
    When I check out the cart
    Then an order should be created with total price 11.5
    And the order status should be 'InProgress'

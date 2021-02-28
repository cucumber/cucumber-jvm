Feature: Shopping

  Scenario: Give correct change
    Given the following groceries:
      | name  | price |
      | milk  | 9     |
      | bread | 7     |
      | soap  | 5     |
    When I pay 25.0 USD
    Then my change should be 4

  Scenario: Count shopping list cost
    Given the following shopping list:
    """shopping_list
    milk
    bread
    coffee
    """
    And the shop has following groceries:
      | name   | price |
      | milk   | 9     |
      | bread  | 7     |
      | coffee | 2     |
      | soap   | 5     |
    When I count shopping price
    Then price would be 18

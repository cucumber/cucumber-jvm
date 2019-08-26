Feature: Cukes

  Scenario: in the belly
    Given I have 4 "cukes" in my belly
    Then I am "happy"

  Scenario: Int in the belly
    Given I have eaten an int 100
    Then I should have one hundred in my belly

  Scenario: Long in the belly
    Given I have eaten a long 100
    Then I should have long one hundred in my belly

  Scenario: String in the belly
    Given I have eaten "numnumnum"
    Then I should have numnumnum in my belly

  Scenario: Double in the belly
    Given I have eaten 1.5 doubles
    Then I should have one and a half doubles in my belly

  Scenario: Float in the belly
    Given I have eaten 1.5 floats
    Then I should have one and a half floats in my belly

  Scenario: Short in the belly
    Given I have eaten a short 100
    Then I should have short one hundred in my belly

  Scenario: Byte in the belly
    Given I have eaten a byte 2
    Then I should have two byte in my belly

  Scenario: BigDecimal in the belly
    Given I have eaten 1.5 big decimals
    Then I should have one and a half big decimals in my belly

  Scenario: BigInt in the belly
    Given I have eaten 10 big int
    Then I should have a ten big int in my belly

  Scenario: Char in the belly
    Given I have eaten char 'C'
    Then I should have character C in my belly

  Scenario: Boolean in the belly
    Given I have eaten boolean true
    Then I should have truth in my belly

  Scenario: DataTable in the belly
    Given I have the following foods :
      | FOOD   | CALORIES |
      | cheese |      500 |
      | burger |     1000 |
      | fries  |      750 |
    Then I am "definitely happy"
    And have eaten 2250.0 calories today

  Scenario: DataTable with args in the belly
    Given I have a table the sum of all rows should be 400 :
      | ROW |
      |  20 |
      |  80 |
      | 300 |

  Scenario: Argh! a snake - to be custom mapped
    Given I see in the distance ... =====>
    Then I have a snake of length 6 moving east
    And I see in the distance ... <====================
    Then I have a snake of length 21 moving west

  Scenario: Custom object with string constructor
    Given I have a person Bob
    Then he should say "Hello, I'm Bob!"

  Scenario: Custom objects in the belly
    Given I have eaten the following cukes
      | Color | Number |
      | Green |      1 |
      | Red   |      3 |
      | Blue  |      2 |
    Then I should have eaten 6 cukes
    And they should have been Green, Red, Blue

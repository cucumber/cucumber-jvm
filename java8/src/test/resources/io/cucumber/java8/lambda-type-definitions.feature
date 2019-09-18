Feature: Lambda type definition

  Scenario: define docstring type by lambda
    Given docstring, defined with lambda
    """doc
    really long docstring
    """

  Scenario: define data table type by lambda
    Given data table, defined with lambda
      |name  | surname       | famousBook          |
      |Fedor | Dostoevsky    |Crime and Punishment |

  Scenario: define parameter type by lambda
    Given stringbuilder parameter, defined by lambda
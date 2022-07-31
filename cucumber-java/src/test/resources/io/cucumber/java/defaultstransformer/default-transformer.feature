Feature: Datatable

  Scenario: Convert a table to a generic list via default transformer
    Given a list of authors in a table
      | firstName   | lastName | birthDate  |
      | Annie M. G. | Schmidt  | 1911-03-20 |
      | Roald       | Dahl     | 1916-09-13 |

  Scenario: Convert a table with title case headers to a single object via the default transformer
    Given a table with title case headers
      | First Name  | last Name | Birth date |
      | Annie M. G. | Schmidt   | 1911-03-20 |
      | Roald       | Dahl      | 1916-09-13 |


  Scenario: Convert a table to a single object via the default transformer

    Given a single currency in a table
      | EUR |

  Scenario: Convert an anonymous parameter to a single object via default transformer

    Given a currency in a parameter EUR

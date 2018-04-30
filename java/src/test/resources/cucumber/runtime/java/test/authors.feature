Feature: Authors and tables

  Scenario: Some authors and tables
    Given a list of authors in a table
      | firstName   | lastName | birthDate  |
      | Annie M. G. | Schmidt  | 1911-03-20 |
      | Roald       | Dahl     | 1916-09-13 |

    Given a list of authors in a transposed table
      | firstName | Annie M. G. | Roald      |
      | lastName  | Schmidt     | Dahl       |
      | birthDate | 1911-03-20  | 1916-09-13 |

    Given a single author in a table
      | firstName   | lastName | birthDate  |
      | Annie M. G. | Schmidt  | 1911-03-20 |

    Given a single author in a transposed table
      | firstName | Annie M. G. |
      | lastName  | Schmidt     |
      | birthDate | 1911-03-20  |


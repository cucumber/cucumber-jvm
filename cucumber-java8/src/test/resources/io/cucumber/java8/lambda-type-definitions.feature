Feature: Lambda type definition

  Scenario: define docstring type by lambda
    Given docstring, defined by lambda
    """doc
    really long docstring
    """

  Scenario: define single entry data table type by lambda
    Given single entry data table, defined by lambda
      | name  | surname    | famousBook           |
      | Fedor | Dostoevsky | Crime and Punishment |

  Scenario: define data table by row transformer
    Given data table, defined by lambda row transformer
      | book                 | main character |
      | Crime and Punishment | Raskolnikov    |
      | War and Peace        | Bolkonsky      |

  Scenario: define data table by cell transformer
    Given data table, defined by lambda cell transformer
      | book                 | main character |
      | Crime and Punishment | Raskolnikov    |

  Scenario: define data table by table transformer
    Given data table, defined by lambda table transformer
      | type    | main character |
      | tragedy | Raskolnikov    |
      | novel   | Bolkonsky      |

  Scenario: define data table type by lambda
    Given data table, defined by lambda
      | name  | surname    | famousBook           |
      | Fedor | Dostoevsky | Crime and Punishment |
      | Lev   | Tolstoy    | War and Peace        |

  Scenario: define parameter type by lambda
    Given string builder parameter, defined by lambda

  Scenario: define Point parameter type by lambda
    Given balloon coordinates 123,456, defined by lambda

  Scenario: define multi argument parameter type by lambda
    Given kebab made from mushroom, meat and veg, defined by lambda

  Scenario: define default parameter transformer by lambda
    Given kebab made from anonymous meat, defined by lambda

  Scenario: define default data table cell transformer by lambda
    Given default data table cells, defined by lambda
      | Kebab |

  Scenario: define default data table entry transformer by lambda
    Given default data table entries, defined by lambda
      | dinner |
      | Kebab  |

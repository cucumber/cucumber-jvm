Feature: tables

  Scenario: a key-value table
    Given I have a kv table:
      | my-first-key | 1          |
      | another-key  | "a string" |
      | yak          | :a-kw      |
    Then the clojure literal equivalent should be:
    """
      {:my-first-key 1, :another-key "a string", :yak :a-kw}
    """

  Scenario: a table
    Given I have a table with its keys in a header row:
      | id | name  | created-at    |
      | 55 | "foo" | 1293884100000 |
      | 56 | "bar" | 1293884100000 |
    Then the clojure literal equivalent should be:
    """
      [{:id 55, :name "foo", :created-at 1293884100000}
       {:id 56, :name "bar", :created-at 1293884100000}]
    """

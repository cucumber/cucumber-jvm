# Cucumber-Clojure

Cucumber-Clojure is a Cucumber implementation for [Clojure](http://www.clojure.org/).

This document is the reference for features that are specific to Cucumber-Clojure.

Please see the [general reference](https://cucumber.io/docs/reference) for features that are common to all Cucumber implementations.

## Step Definitions

Clojure step definitions are defined by using the provided [macros](http://clojure.org/macros). For example:

```clojure
(use 'clojure-cukes.core)
(use 'clojure.test)

(Given #"^I have (\d+) big cukes in my belly$" [cukes]
       (println cukes))
```

You can use a [DataTable](https://cucumber.io/docs/reference#data-table) to define a list:

```
Given I have a table with its keys in a header row:
  | id | name  | created-at    |
  | 55 | "foo" | 1293884100000 |
  | 56 | "bar" | 1293884100000 |
```

Simply declare the following:

```clojure
(Given #"^I have a table with its keys in a header row:$" [data]
  (reset! most-recent (table->rows data)))
```

In this case, the DataTable is flattened to a vector of hashes

```clojure
[{:id 55, :name "foo", :created-at 1293884100000}
{:id 56, :name "bar", :created-at 1293884100000}]
```

before invoking the step definition.

## Running

There are several ways to run scenarios with Cucumber-Clojure:
* lein-cucumber
* JunitRunner

### lein-cucumber

[Leiningen](http://leiningen.org/) uses [clojure.test](TODO) to run Cucumber. All you need is a single entry point:

```clojure
(ns clojure-cukes.test.core
  (:use [clojure-cukes.core])
  (:use [clojure.test]))

(deftest run-cukes
  (. cucumber.api.cli.Main (main (into-array ["--plugin" "pretty" "--glue" "test/features/step_definitions" "test/features"]))))

```

You then need to add `[lein-cucumber "1.0.2"]` to `:plugins` in your project.clj. This allows you to run all Cucumber features with `lein cucumber`

### JUnitRunner

The instructions for the JUnitRunner can be found [here](https://cucumber.io/docs/reference/jvm#junit-runner)

## Miscellaneous

This module needs further documentation. The following examples show supported features:

* [examples/clojure_cukes](https://github.com/cucumber/cucumber-jvm/tree/master/examples/clojure_cukes)
* [clojure/src/test/resources/cucumber/runtime/clojure](https://github.com/cucumber/cucumber-jvm/tree/master/clojure/src/test/resources/cucumber/runtime/clojure).

Contributions are most welcome.

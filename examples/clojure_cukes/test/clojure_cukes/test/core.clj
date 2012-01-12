(ns clojure-cukes.test.core
  (:use [clojure-cukes.core])
  (:use [clojure.test]))

(deftest run-cukes
  (. cucumber.cli.Main (main (into-array ["--glue" "test/features/step_definitions" "test/features"]))))

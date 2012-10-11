(ns clojure-cukes.test.core
  (:use [clojure-cukes.core])
  (:use [clojure.test]))

(deftest run-cukes
  (. cucumber.api.cli.Main (main (into-array ["--format" "pretty" "--glue" "test/features/step_definitions" "test/features"]))))

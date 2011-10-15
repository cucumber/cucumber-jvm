(ns clojure_cukes.test.core
  (:use [clojure-cukes.core])
  (:use [clojure.test]))

(deftest run-cukes
  (. cucumber.cli.Main (main (into-array ["--glue" "features/step_definitions" "features"]))))

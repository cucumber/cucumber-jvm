(ns clojure_cukes.test.core
  (:use [clojure_cukes.core])
  (:use [clojure.test]))

(deftest run-cukes
  (. cucumber.cli.Main (main (into-array ["--glue" "features/step_definitions" "features"]))))

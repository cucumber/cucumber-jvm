(ns clojure-cukes.test.core
  (:use [clojure-cukes.core])
  (:use [clojure.test])
  (:use [leiningen.cucumber])
  (:import [cucumber.api.cli Main]))

(deftest run-cukes
  (. cucumber.api.cli.Main (main (into-array ["--format" "pretty" "--glue" "test" "test/features"]))))

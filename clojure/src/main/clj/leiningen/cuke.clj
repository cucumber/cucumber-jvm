(ns leiningen.cuke
  (:require [leiningen.compile :as lc]))

(defn cuke
  "runs cucumber"
  [project]
  ;; main calls System/exit kind of gross
  (lc/eval-in-project
   project
   `(cucumber.cli.Main/main
     (into-array String
                 ["--format" "pretty"
                  "--glue" "test/cucumber" "test/cucumber/features"]))))

(ns leiningen.cuke)

(defn cuke
  "runs cucumber"
  [project]
  ;; main calls System/exit kind of gross
  (cucumber.cli.Main/main
   (into-array String
               ["--format" "pretty"
                "--glue" "resources/" "resources/features"])))

(defproject clojure_cukes "1.0.X"
  :description "A demo of Cucumber with Clojure and Leiningen"
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :plugins [[lein-cucumber "1.0.2"]]
  :cucumber-feature-paths ["test/features/"]
  :profiles
  {:dev
   {:dependencies [[lein-cucumber "1.0.2"]]}}
)

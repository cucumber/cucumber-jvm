(defproject clojure_cukes "1.0.0.RC2-SNAPSHOT"
  :description "A demo of Cucumber with Clojure and Leiningen"
  :dependencies [[org.clojure/clojure "1.4.0-alpha3"]]
  :plugins [[lein-cucumber "1.0.0.M1-SNAPSHOT"]]
  :cucumber-feature-paths ["test/features/"])

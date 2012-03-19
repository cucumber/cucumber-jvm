(ns leiningen.cuke
  (:require [leiningen.compile :as lc]
            [leiningen.core :as c]))

(defn cuke
  "runs cucumber"
  [project]
  ;; basically a reimplimentation of cli.Main that doesn't annoyingly
  ;; call System/exit
  (lc/eval-in-project
   project
   `(let [~'runtime (cucumber.runtime.Runtime.
                     (list* ["test/cucumber"])
                     (cucumber.io.FileResourceLoader.) false)
          mformatter# (doto (cucumber.formatter.MultiFormatter.)
                        (.add (.createFormatter
                               (cucumber.formatter.FormatterFactory.)
                               "pretty" System/out)))
          formatter#  (.formatterProxy mformatter#)]
      (.run ~'runtime
            (list* ["test/cucumber/features"])
            (list)
            formatter#
            (.reporterProxy mformatter#))
      (.done formatter#)
      (println)
      ~(when-not c/*interactive?*
         `(System/exit (.exitStatus ~'runtime))))))

(ns cucumber.runtime.clojure.belly)

(def cukes (ref (vector)))

(defn eat [num]
  (dosync (alter cukes conj num)))

(defn last-meal []
  (last @cukes))


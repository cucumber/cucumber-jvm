(ns clojure-cukes.core)

(def belly (atom []))

(defn mood []
  (let [cukes (->> @belly
                   (filter #(= "cukes" %))
                   count)]
    (cond
     (> cukes 3) :happy
     (= cukes 2) :meh
     (= cukes 1) :sad
     :else :hungry)))

(defn eat [things]
  (swap! belly concat things))

(use 'cucumber.runtime.clojure.belly)

(def some-state (atom "'Before' hasn't run."))

(Before []
  (reset! some-state "'Before' has run.")
  (println "Executing 'Before'."))

(After []
  (println (str "Executing 'After' " @some-state)))

(Given #"^I have (\d+) cukes in my belly$" [cuke-count]
  (eat (Float. cuke-count)))

(Given #"^I have this many cukes in my belly:$" [cuke-table]
  (doseq [x (.raw cuke-table)] (eat (Float. (first x)))))

(When #"^there are (\d+) cukes in my belly$" [expected]
  (assert (= (last-meal) (Float. expected))))
    
(Then #"^the (.*) contains (.*)$" [container ingredient]
  (assert (= "glass" container)))

(When #"^I add (.*)$" [liquid]
  (assert (= "milk" liquid)))

(use 'cucumber.runtime.clojure.belly)

(Given #"^I have (\d+) cukes in my belly$"
  #(eat (Float. %1)))

(When #"^there are (\d+) cukes in my belly$"
  (fn [expected]
    (assert (= (last-meal) (Float. expected)))))
(use 'cucumber.runtime.clojure.belly)

(Given #"^I have (\d+) cukes in my belly$"
  #(eat (Float. %1)))

(When #"^there are (\d+) cukes in my belly$"
  (fn [expected]
    (assert (= (last-meal) (Float. expected)))))
    
(Then #"^the (.*) contains (.*)$"
  (fn [container, ingredient]
    (assert (= "glass" container))))

(When #"^I add (.*)$"
  (fn [liquid]
    (assert (= "milk" liquid))))
